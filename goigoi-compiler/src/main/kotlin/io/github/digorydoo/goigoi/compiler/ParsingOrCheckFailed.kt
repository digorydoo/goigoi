package io.github.digorydoo.goigoi.compiler

abstract class ParsingOrCheckFailed(msg: String, cause: Throwable? = null): Exception(msg, cause)

class ParsingFailed(msg: String?, cause: Throwable? = null): ParsingOrCheckFailed(msg ?: "Parsing failed", cause)
class CheckFailed(msg: String?, cause: Throwable? = null): ParsingOrCheckFailed(msg ?: "Check failed", cause)
