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

package com.wolery.owl.gui

//****************************************************************************

import com.wolery.owl.utils.event.foo

import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region.USE_PREF_SIZE
import javafx.scene.layout.StackPane
import javafx.scene.text.Text

//****************************************************************************

class Bead(text: String,style: String = "bead") extends StackPane
{
  val t = new Text(text)

  t.setStyle("-fx-fill:inherit;-fx-stroke:inherit;-fx-stroke-width:inherit;")

  getStyleClass.add(style)
  getChildren.add(t)

  setMinSize(USE_PREF_SIZE,USE_PREF_SIZE)
  setMaxSize(USE_PREF_SIZE,USE_PREF_SIZE)
  setPrefSize(30,30)

  setOnMouseClicked((e: MouseEvent) ⇒ println("clicked!"))
}

//****************************************************************************
