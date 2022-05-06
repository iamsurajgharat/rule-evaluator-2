package io.github.iamsurajgharat
package ruleevaluator
package services

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[ConfServiceImpl])
trait ConfService {
    val totalNumberOfShards: Int
}


class ConfServiceImpl extends ConfService {
    val totalNumberOfShards: Int = 10
}