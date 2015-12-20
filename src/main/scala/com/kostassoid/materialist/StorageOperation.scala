package com.kostassoid.materialist

trait StorageOperation {
  def key: String
}

case class Upsert(key: String, value: String) extends StorageOperation

case class Delete(key: String) extends StorageOperation
