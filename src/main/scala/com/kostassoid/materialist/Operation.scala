package com.kostassoid.materialist

trait Operation {
  def key: String
  def stream: String
}

case class Upsert(key: String, stream: String, value: String) extends Operation

case class Delete(key: String, stream: String) extends Operation
