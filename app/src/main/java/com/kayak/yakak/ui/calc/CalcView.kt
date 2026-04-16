package com.kayak.yakak.ui.calc

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class CalculatorState(
    val number1: String = "",
    val number2: String = "",
    val operation: String? = null
)

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Decimal : CalculatorAction()
    object Calculate : CalculatorAction()
    data class Operation(val operation: String) : CalculatorAction()
}


@Composable
fun CalcView() {
    var state by remember { mutableStateOf(CalculatorState()) }

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> {
                if (state.operation == null) {
                    if (state.number1.length < 8) {
                        state = state.copy(number1 = state.number1 + action.number)
                    }
                } else {
                    if (state.number2.length < 8) {
                        state = state.copy(number2 = state.number2 + action.number)
                    }
                }
            }
            is CalculatorAction.Operation -> {
                if (state.number1.isNotBlank()) {
                    state = state.copy(operation = action.operation)
                }
            }
            is CalculatorAction.Calculate -> {
                val num1 = state.number1.toDoubleOrNull()
                val num2 = state.number2.toDoubleOrNull()
                if (num1 != null && num2 != null && state.operation != null) {
                    val result = when (state.operation) {
                        "+" -> num1 + num2
                        "-" -> num1 - num2
                        "×" -> num1 * num2
                        "÷" -> if (num2 != 0.0) num1 / num2 else return
                        else -> return
                    }
                    // Formatage pour éviter le .0 inutile
                    val formattedResult = if (result % 1.0 == 0.0) {
                        result.toLong().toString()
                    } else {
                        result.toString().take(10) // Limite la longueur
                    }
                    state = state.copy(number1 = formattedResult, number2 = "", operation = null)
                }
            }
            is CalculatorAction.Clear -> {
                state = CalculatorState()
            }
            is CalculatorAction.Delete -> {
                when {
                    state.number2.isNotBlank() -> state = state.copy(number2 = state.number2.dropLast(1))
                    state.operation != null -> state = state.copy(operation = null)
                    state.number1.isNotBlank() -> state = state.copy(number1 = state.number1.dropLast(1))
                }
            }
            is CalculatorAction.Decimal -> {
                if (state.operation == null && !state.number1.contains(".") && state.number1.isNotBlank()) {
                    state = state.copy(number1 = state.number1 + ".")
                } else if (state.operation != null && !state.number2.contains(".") && state.number2.isNotBlank()) {
                    state = state.copy(number2 = state.number2 + ".")
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Affichage de l'équation et du résultat
            Text(
                text = buildString {
                    append(state.number1)
                    append(state.operation ?: "")
                    append(state.number2)
                }.ifEmpty { "0" },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 80.sp, // Style expressif imposant
                    fontWeight = FontWeight.Light
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grille de boutons
            val buttonSpacing = 12.dp

            Column(verticalArrangement = Arrangement.spacedBy(buttonSpacing)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("AC", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, Modifier.weight(2f)) { onAction(CalculatorAction.Clear) }
                    CalculatorButton("⌫", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, Modifier.weight(1f)) { onAction(CalculatorAction.Delete) }
                    CalculatorButton("÷", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { onAction(CalculatorAction.Operation("÷")) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("7", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Number(7)) }
                    CalculatorButton("8", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Number(8)) }
                    CalculatorButton("9", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Number(9)) }
                    CalculatorButton("×", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { onAction(CalculatorAction.Operation("×")) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("4", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Number(4)) }
                    CalculatorButton("5", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Number(5)) }
                    CalculatorButton("6", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Number(6)) }
                    CalculatorButton("-", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { onAction(CalculatorAction.Operation("-")) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("1", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Number(1)) }
                    CalculatorButton("2", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Number(2)) }
                    CalculatorButton("3", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Number(3)) }
                    CalculatorButton("+", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { onAction(CalculatorAction.Operation("+")) }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalculatorButton("0", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(2f)) { onAction(CalculatorAction.Number(0)) }
                    CalculatorButton(".", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) { onAction(CalculatorAction.Decimal) }
                    CalculatorButton("=", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, Modifier.weight(1f)) { onAction(CalculatorAction.Calculate) }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(if (symbol == "0" || symbol == "AC") 2f else 1f) // Ratio plus large pour le 0 et AC
            .clip(CircleShape) // Arrondi maximum typique du M3 Expressive
            .background(backgroundColor)
            .clickable { onClick() }
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineMedium,
            color = contentColor
        )
    }
}