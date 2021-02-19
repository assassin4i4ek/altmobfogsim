package api2

interface AD {
    val a: String
}

interface BD: AD {
    val b: String
    override val a: String get() = b
}

interface CD: AD {
    val c: Int
    override val a: String get() = c.toString()
}

interface DD: BD, CD {
    val d: String
    override val a: String
        get() = d + super<BD>.a + super<CD>.a
}
