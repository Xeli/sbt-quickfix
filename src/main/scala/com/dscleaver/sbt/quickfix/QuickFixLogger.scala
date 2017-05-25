package com.dscleaver.sbt.quickfix

import sbt._

object QuickFixLogger {
  def append(output: File, prefix: String, message: String): Unit =
    IO.append(output, "[%s] %s\n".format(prefix, message.replace('\n', ' ')))

  def append(output: File, prefix: String, file: File, line: Int, message: String): Unit =
    append(output, prefix, "%s:%d: %s".format(file, line, message))
}

class QuickFixLogger(val output: File, vimExec: String, enableServer: Boolean) extends BasicLogger {
  import QuickFixLogger._
  import VimInteraction._

  def log(level: Level.Value, message: => String): Unit = level match {
    case Level.Info => handleInfoMessage(message)
    case Level.Error => handleErrorMessage(message)
    case Level.Warn => handleWarnMessage(message)
    case _ => handleDebugMessage(message)
  }

  def handleDebugMessage(message: String): Unit = {
    if (message.toLowerCase.contains("initial source changes:")) {
      resetFile()
      notifyVim()
    }
    if (enableServer && message.toLowerCase.contains("compilation failed")) {
      notifyVim()
    }
  }

  def resetFile() {
    IO.delete(output)
    IO.touch(List(output))
  }

  def notifyVim() {
    call(vimExec, "<esc>:cgetfile %s<cr>".format(output.toString))
    call(vimExec, ":cw<cr>")
    ()
  }

  def handleInfoMessage(message: String): Unit = ()

  def handleErrorMessage(message: String): Unit = append(output, "error", message)

  def handleWarnMessage(message: String): Unit = append(output, "warn", message)

  def control(event: ControlEvent.Value, message: => String): Unit = ()

  def logAll(events: Seq[LogEvent]): Unit = ()

  def success(message: => String): Unit = ()

  def trace(t: => Throwable): Unit = ()
}
