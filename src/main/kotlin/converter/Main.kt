package converter

fun main() {
    while (true) {
        println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")
        val userChoice = readln()
        if (userChoice == "/exit") {
            break
        }
        val conversionBases = userChoice.split("\\s+".toRegex()).map {
            it.trim().toBigInteger()
        }
        check(conversionBases.size == 2) {
            "You must provide {source base} and {target base}"
        }
        val (sourceBase, targetBase) = conversionBases
        while (true) {
            println("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back)")
            val userInput = readln().trim().uppercase()
            if (userInput == "/BACK") {
                break
            }
            val numberConverter = NumberConverter(sourceBase, targetBase)
            println("Conversion result: ${numberConverter.convert(userInput)}")
        }
    }
}

