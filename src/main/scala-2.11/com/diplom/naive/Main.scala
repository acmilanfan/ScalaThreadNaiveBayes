package com.diplom.naive

import java.util.concurrent.Executors

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Await, ExecutionContext}

object Main extends App {

  val learning = new Learning()
  val fileReader = new FileReader()

  if (args.length == 4) {
    val threadNum = args(3).toInt
    implicit val executor = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(threadNum))
    //worker
//    def futureWorker(clazz: String, attrs: String) = Future[Double] {
//      learning.classifier.calculateProbability(clazz, attrs)
//    }
    var results = List[(String, Double)]()
    fileReader.read(args(2), learning)
    val startTime = System.currentTimeMillis()

    var futureList = List[Future[Double]]()

    learning.model.classes.foreach(clazz => {
      val future = Future[Double] {
        learning.classifier.calculateProbability(clazz, fileReader.readAttrs(args(0)))
      }
      future.onSuccess {
        case value => results = (clazz, value) :: results
      }
      futureList = futureList :+ future
    })
    //отправляем данные на расчет вероятностей для каждого класса
//    learning.model.classes.foreach(clazz => futureWorker(clazz, fileReader.readAttrs(args(0))).onSuccess {
//      case value => results = (clazz, value) :: results
//    })

    val futureOfFutures = Future.sequence(futureList)
    Await.ready(futureOfFutures, Duration.Inf)

    val stopTime = System.currentTimeMillis()
    results = results.sortBy(_._2)

    println(s"Время работы - ${stopTime - startTime} ms")
    println(s"Наиболее вероятный класс - ${results.last._1}")
    println(s"Вероятность - ${learning.normProb(results, results.last._2)}")

  } else {
    println("Некорректные параметры запуска программы.")
    println("Необходимо использовать данный формат: <AtrPath> <Object Name> <Data Path>")
  }
}
