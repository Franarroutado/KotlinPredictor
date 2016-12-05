package com.xabarin.ui

import com.jfoenix.controls.JFXTextField
import com.xabarin.dao.complete

import javafx.application.Application
import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import javafx.stage.Stage

import nl.komponents.kovenant.jfx.configureKovenant
import nl.komponents.kovenant.task
import org.reactfx.EventStreams

import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration

import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.Executors

import kotlin.system.measureTimeMillis

val thread = Executors.newSingleThreadExecutor()

class App : Application() {

    val environments = ConcurrentSkipListSet<com.xabarin.dao.Box>()

    override fun start(stage: Stage) {
        configureKovenant()

        val txtBox: JFXTextField = JFXTextField().apply {
            labelFloatProperty()
            promptText ="type a box"
        }


//        val txtEnv: JFXTextField = JFXTextField().apply {
//            labelFloatProperty()
//            promptText ="type a env"
//        }

        val completitionList = ListView<String>()

//        val vbox = VBox(txtBox, txtEnv, completitionList).apply {
        val vbox = VBox(txtBox, completitionList).apply {
            spacing = 30.0
            style = "-fx-background-color:WHITE;-fx-padding:40;"
        }

        stage.title = "Predictor"

//        val environmentsEventStreams = EventStreams.valuesOf(txtEnv.textProperty()).forgetful()
//        val fewerEnvironmentsEventStreams = environmentsEventStreams.successionEnds(Duration.ofMillis(100))
//        val environmentsInBackGround = fewerEnvironmentsEventStreams.threadBridgeFromFx(thread)
//        val environmentCompetitions = environmentsInBackGround.map {
//            environments.complete(it, false)
//        }
//        val environmentInForeground = environmentCompetitions.threadBridgeToFx(thread)
//
//        val environmentLastResult = environmentInForeground.map{ FXCollections.observableArrayList(it) }
//                .toBinding(FXCollections.emptyObservableList())


        val edits = EventStreams.valuesOf(txtBox.textProperty()).forgetful()
        val fewerEdits = edits.successionEnds(Duration.ofMillis(100)) //.filter { it.isNotBlank() }

        val inBackground = fewerEdits.threadBridgeFromFx(thread)
        val completions = inBackground.map{
            environments.complete(it)
        }

        val inForeground = completions.threadBridgeToFx(thread)

        val lastResult = inForeground.map{ FXCollections.observableArrayList(it) }
                .toBinding(FXCollections.emptyObservableList())

        completitionList.itemsProperty().bind(lastResult)

        val guard = edits.suspend()
//        val guardEnv = lastResult.suspend()
        task {
            load()
        } success {
            println("successfully load")
            guard.close()
//            guardEnv.close()
        } fail {
            throw RuntimeException(it)
        }

        stage.scene = Scene(vbox)
        stage.show()
    }

    private fun load() {
        val TAB = '\t'
        val path = Paths.get("./boxes.txt")
        val t = measureTimeMillis {
            Files.lines(path).parallel().forEach { line ->
                val environment = line.substringBefore(TAB).replace(TAB, ' ').toLowerCase()
                val box = line.substringAfter(TAB).substringAfter(TAB).substringBefore(TAB).replace(TAB, ' ').toLowerCase()
                environments.add(com.xabarin.dao.Box(environment, box))
                println(box)
            }
        }
        println("Loaded file in $t msecs")
        println(environments)
    }


}

fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
    System.exit(0)
}

