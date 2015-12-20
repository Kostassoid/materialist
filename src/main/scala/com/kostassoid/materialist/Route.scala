package com.kostassoid.materialist

case class Route(source: Source, target: Target, operationPredicate: StorageOperation ⇒ Boolean)