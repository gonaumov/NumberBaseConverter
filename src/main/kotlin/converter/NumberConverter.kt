package converter

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

private const val CAPITAL_LETTER_A = 64

private const val ROUNDING_PRECISION = 6

class NumberConverter(
    private val sourceBase: BigInteger,
    private val targetBase: BigInteger
) {

    private fun convertFromDecimal(decimalNumber: String): String {
        var tempNumber = decimalNumber.toBigInteger()
        var result = ""
        do {
            val currentResult = tempNumber.divideAndRemainder(targetBase)[1]
            result += symbolToText(currentResult)
            tempNumber = tempNumber.divide(targetBase)
        } while (tempNumber >= BigInteger.ONE)

        return result.reversed()
    }

    private fun symbolToText(currentResult: BigInteger): String {
        return if (currentResult < BigInteger.valueOf(10)) {
            currentResult.toString()
        } else {
            ('A' + (currentResult.toInt() - 10)).toString()
        }
    }

    private fun convertIntegralPartToDecimal(numberInput: String): String {
        var result = BigInteger.ZERO
        for (i in numberInput.indices) {
            val leftIndex = (i - numberInput.lastIndex) * -1
            val currentSymbol = numberInput[i]
            result += symbolToIntegerValue(currentSymbol, leftIndex)
        }
        return result.toString()
    }

    private fun convertFractionalPartToDecimal(numberInput: String): String {
        var result = BigDecimal.ZERO
        for (i in numberInput.indices) {
            val currentSymbol = numberInput[i]
            result += symbolToFractionIntegerValue(currentSymbol, i + 1)
        }
        return result.toString()
    }

    private fun symbolToIntegerValue(currentSymbol: Char, leftIndex: Int): BigInteger {
        return if (currentSymbol in '\u0030'..'\u0039') {
            currentSymbol.toString().toBigInteger().multiply(sourceBase.pow(leftIndex))
        } else {
            val number = 9 + (currentSymbol.code - CAPITAL_LETTER_A)
            number.toBigInteger().multiply(sourceBase.pow(leftIndex))
        }
    }

    private fun symbolToFractionIntegerValue(currentSymbol: Char, index: Int): BigDecimal {
        return if (currentSymbol in '\u0030'..'\u0039') {
            currentSymbol.toString().toBigDecimal()
                .multiply("1".toBigDecimal().divide(sourceBase.toBigDecimal().pow(index), ROUNDING_PRECISION, RoundingMode.UP))
        } else {
            val number = 9 + (currentSymbol.code - CAPITAL_LETTER_A)
            number.toBigDecimal()
                .multiply("1".toBigDecimal().divide(sourceBase.toBigDecimal().pow(index), ROUNDING_PRECISION, RoundingMode.UP))
        }
    }

    private fun convertIntegerPart(input: String): String {
        val decimalNumber = convertIntegralPartToDecimal(input)
        return convertFromDecimal(decimalNumber)
    }

    private fun convertFractionPart(input: String, targetBase: Int): String {
        if (input == "false") {
            return ""
        }
        val convertedInput = convertFractionalPartToDecimal(input)
        val oneDot = "."
        var result = oneDot
        var inputHandler = convertedInput.toBigDecimal()
        while (inputHandler.signum() != 0 && result.length < ROUNDING_PRECISION) {
            val multiplicationResult: BigDecimal = inputHandler.multiply(BigDecimal.valueOf(targetBase.toLong()))
            val (integerPart, fractionPart) = multiplicationResult.divideAndRemainder(BigDecimal.ONE)
            inputHandler = fractionPart
            result += symbolToText(integerPart.setScale(0).toBigInteger())
        }
        return if (result == oneDot) {
            oneDot + input
        } else {
            result.padEnd(ROUNDING_PRECISION, '0')
        }
    }

    fun convert(input: String): String {
        val (integerPart, fractionPart) = input.let { part ->
            when {
                part.contains(".") -> part.split(".").map {
                    it.trim()
                }

                else -> listOf(part, "false")
            }
        }

        return convertIntegerPart(integerPart) + convertFractionPart(fractionPart, targetBase.toInt())
    }
}