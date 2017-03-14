//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose : Displays the file format information of a MIDI file.
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*
//*https://www.midi.org/specifications/item/table-1-summary-of-midi-message
//****************************************************************************

package com.wolery.owl.utils

//****************************************************************************

import java.io.PrintStream
import javax.sound.midi._
import com.wolery.owl.core._
import javax.sound.midi.SysexMessage._

//****************************************************************************

final class DumpReceiver(m_out: PrintStream = System.out) extends Receiver
{
  def close(): Unit =
  {}

  def send(mm: MidiMessage,ts: Long): Unit =
  {
    if (ts != -1)
    {
      m_out.print(s"$ts ")
    }

    mm match
    {
      case m: MetaMessage                       ⇒ onMetaMessage(m)
      case m: SysexMessage                      ⇒ onSysexMessage(m)
      case m: ShortMessage if isSystemMessage(m)⇒ onSystemMessage(m)
      case m: ShortMessage                      ⇒ onChannelMessage(m)
      case _                                    ⇒ m_out.print("unknown message type")
    }

    m_out.println()
  }

  private
  def isSystemMessage (mm: ShortMessage): Boolean =
  {
    mm.getCommand == 0xF0
  }

  private
  def isChannelMessage(mm: ShortMessage): Boolean =
  {
    mm.getCommand != 0xF0
  }

  def onMetaMessage(mm: MetaMessage): Unit =
  {
    def i(i: ℕ): ℤ   = mm.getData.apply(i) & 0xFF
    def text: String = new String(mm.getData)

    def key : String =
    {
      val keys = Seq("C♭","G♭","D♭","A♭","E♭","B♭","F","C","G","D","A","E","B","F♯","C♯")
      val mode = Seq(" maj"," min")

      keys(7 + i(0)) + mode(i(1))
    }

    def tempo: Float =
    {
      // tempo in microseconds per beat
      val mspb = (i(0) << 16) | (i(1) <<  8) | i(2)
      val mspq = if (mspb <= 0) 60e6f / 0.1f
                 else           60e6f / mspb
      // truncate it to 2 digits after dot
      Math.round(mspq * 100.0F) / 100.0F
    }

    def offset: String        = s"${i(0)}:${i(1)}:${i(2)}:${i(3)}:${i(4)}"

    def timesig: String = s"${i(0)}/${1<<i(1)}, MIDI clocks per metronome tick: ${i(2)}, 1/32 per 24 MIDI clocks: ${i(3)}"

    def sequence: ℤ   = (i(0) << 8) | i(1)

    mm.getType match
    {
      case 0x00 ⇒ m_out.print(s"sequence number: $sequence")
      case 0x01 ⇒ m_out.print(s"text event: $text")
      case 0x02 ⇒ m_out.print(s"copyright: $text")
      case 0x03 ⇒ m_out.print(s"sequence/track name: $text")
      case 0x04 ⇒ m_out.print(s"instrument $text: ")
      case 0x05 ⇒ m_out.print(s"lyric: $text")
      case 0x06 ⇒ m_out.print(s"marker: $text")
      case 0x07 ⇒ m_out.print(s"cue point: $text")
      case 0x20 ⇒ m_out.print(s"channel prefix: ${i(0)}")
      case 0x2F ⇒ m_out.print(s"end of track")
      case 0x51 ⇒ m_out.print(s"tet tempo: $tempo bpm")
      case 0x54 ⇒ m_out.print(s"SMTPE offset: $offset")
      case 0x58 ⇒ m_out.print(s"Time Signature: $timesig")
      case 0x59 ⇒ m_out.print(s"key signature: $key")
      case 0x7F ⇒ m_out.print("sequencer-specific meta event: "+hex(mm))
      case _    ⇒ m_out.print("unknown meta event: "           +hex(mm))
    }
  }

  private
  def onSysexMessage(mm: SysexMessage): Unit = mm.getStatus match
  {
    case 0xF0 ⇒ m_out.print(s"sysex message[ ${hex(mm)}")
    case 0xF7 ⇒ m_out.print(s"sysex message] ${hex(mm)}")
  }

  private
  def onSystemMessage(mm: ShortMessage): Unit =
  {
    require(isSystemMessage(mm))

    def long = (mm.getData1 & 0x7F) | ((mm.getData2 & 0x7F) << 7)
    def song = f"${mm.getData1}%03d"

    def mtcQuarterFrame =
    {
      mm.getData1 & 0x70 match
      {
        case 0x00 ⇒ m_out.print("frame count LS:   ")
        case 0x10 ⇒ m_out.print("frame count MS:   ")
        case 0x20 ⇒ m_out.print("seconds count LS: ")
        case 0x30 ⇒ m_out.print("seconds count MS: ")
        case 0x40 ⇒ m_out.print("minutes count LS: ")
        case 0x50 ⇒ m_out.print("minutes count MS: ")
        case 0x60 ⇒ m_out.print("hours count LS:   ")
        case 0x70 ⇒ m_out.print("hours count MS:   ")
      }

      m_out.print(mm.getData1 & 0x0F)
    }

    mm.getStatus & 0x0F match
    {
      case 0x0 ⇒ m_out.print(s"sysex[           ")
      case 0x1 ⇒ m_out.print(s"mtc quarter frame mtcQuarterFrame")
      case 0x2 ⇒ m_out.print(s"song position $long")
      case 0x3 ⇒ m_out.print(s"song select $song")
      case 0x6 ⇒ m_out.print(s"tune request     ")
      case 0x7 ⇒ m_out.print(s"sysex]           ")
      case 0x8 ⇒ m_out.print(s"timing clock     ")
      case 0xA ⇒ m_out.print(s"start            ")
      case 0xB ⇒ m_out.print(s"continue         ")
      case 0xC ⇒ m_out.print(s"stop             ")
      case 0xE ⇒ m_out.print(s"active sensing   ")
      case 0xF ⇒ m_out.print(s"system reset     ")
      case  _  ⇒ m_out.print(s"?")
    }
  }

  private
  def onChannelMessage(mm: ShortMessage): Unit =
  {
    require(isChannelMessage(mm))

    def chan = f"ch ${mm.getChannel + 1}%02d"
    def note = f"${Pitch(mm.getData1).toString}%-3s"
    def val1 = f"${mm.getData1}%03d"
    def val2 = f"${mm.getData2}%03d"
    def long = (mm.getData1 & 0x7F) | ((mm.getData2 & 0x7F) << 7)

    m_out.print('[')
    m_out.print(hex(mm))
    m_out.print(']')
    m_out.print(" " * (10 - 3 * mm.getLength))

    mm.getCommand match
    {
      case 0x80 ⇒ m_out.print(s"$chan: note-off   $note ($val2)")
      case 0x90 ⇒ m_out.print(s"$chan: note-on    $note ($val2)")
      case 0xA0 ⇒ m_out.print(s"$chan: p-pressure $note ($val2)")
      case 0xB0 ⇒ m_out.print(s"$chan: controller $val1 ($val2)")
      case 0xC0 ⇒ m_out.print(s"$chan: program    $val1")
      case 0xD0 ⇒ m_out.print(s"$chan: c-pressure $note")
      case 0xE0 ⇒ m_out.print(s"$chan: pitch-bend $long")
      case _    ⇒ m_out.print("unknown message")
    }
  }

  private
  def hex(mm: MidiMessage): String =
  {
    val n = mm.getLength
    val b = mm.getMessage
    val s = new StringBuffer(n * 3)

    for (i ← 0 until 1)
    {
      s.append(f"${b(i)}%02X")
    }

    for (i ← 1 until n)
    {
      s.append(' ');
      s.append(f"${b(i)}%02X")
    }

    s.toString
  }
}

//****************************************************************************
