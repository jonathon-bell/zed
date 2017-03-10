//**************************** Copyright © Jonathon Bell. All rights reserved.
//*
//*
//*  Version : $Header:$
//*
//*
//*  Purpose :
//*
//*
//*  Comments: This file uses a tab size of 2 spaces.
//*
//*
//****************************************************************************

package com.wolery.owl.stringed

import com.wolery.owl._
import com.wolery.owl.core.Pitch
import com.wolery.owl.gui.Bead

import javafx.fxml.{ FXML ⇒ fx }
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{ ColumnConstraints, GridPane, Pane, RowConstraints }
import javafx.util.Duration
import scalafx.Includes.{ jfxDuration2sfx, jfxGridPane2sfx, jfxNode2sfx }
import scalafx.animation._

//****************************************************************************

class StringedController(val instrument: StringedInstrument) extends Controller
{
  @fx
  var root: Pane                   = _
  val rows: Seq[RowConstraints]    = makeRows
  val cols: Seq[ColumnConstraints] = makeCols

//val harm: LayerBuilder = null
//val melo: LayerBuilder = null

  def update(layer: Symbol,chords: Seq[Chord]) =
  {
    val gp = newGrid()

    for {chord ← chords;
         pitch ← chord;
         cell  ← instrument.cells(pitch)}
    {
      val bead = newBead(layer,pitch)

      gp.add(bead,cell.fret,instrument.strings.size-1 - cell.string)
    }

    fade(0,1,1000)(gp)
  }

  def fade(from:Double,to: Double,ms:Int = 2000)(node: Node): Transition =
  {
    new FadeTransition(Duration.millis(ms),node)
    {
      fromValue = 0
      toValue   = 1
      play()
    }
  }

  def newGrid(): GridPane =
  {
    val g = new GridPane
  //g.gridLinesVisible = true
    g.getRowConstraints.addAll   (rows:_*)
    g.getColumnConstraints.addAll(cols:_*)
    root.getChildren.add(g)
    g
  }

  def newBead(layer: Symbol,p: Pitch): Bead = layer match
  {
    case 'background ⇒ new Bead(p.note.toString,"bead-white-text")
    case 'harmony    ⇒ new Bead(p.note.toString,"bead")
    case 'melody     ⇒ new Bead(p.toString,     "bead")
  }

  def makeRows: Seq[RowConstraints] =
  {
    val n = instrument.strings.size
    val h = 100.0 / n

    for (r ← 0 until n) yield
    {
      new RowConstraints(){setPercentHeight(h)}
    }
  }

  def makeCols: Seq[ColumnConstraints] =
  {
    val n = instrument.frets + 1
    val w = 100.0 / n

    for (c ← 0 until n) yield
    {
      new ColumnConstraints(){setPercentWidth(w);setHalignment(javafx.geometry.HPos.CENTER)}
    }
  }
}

//****************************************************************************

trait LayerBuilder
{
  def create: GridPane
}

//****************************************************************************
