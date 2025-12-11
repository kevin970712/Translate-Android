package com.android.sttranslate

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.sttranslate.ui.theme.STTranslateTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb())
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            STTranslateTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RTranslatorStyleScreen()
                }
            }
        }
    }
}

@Composable
fun RTranslatorStyleScreen() {
    // 狀態管理
    var sourceLangCode by remember { mutableStateOf("en") }
    var targetLangCode by remember { mutableStateOf("zh-TW") }
    var inputText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // 翻譯邏輯
    fun performTranslate() {
        if (inputText.isBlank()) return
        focusManager.clearFocus()
        isLoading = true
        scope.launch {
            try {
                val response = NetworkModule.api.translate(source = sourceLangCode, target = targetLangCode, query = inputText)
                resultText = response.translatedText
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_connection), Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            LanguageSelectionBar(
                sourceLang = sourceLangCode,
                targetLang = targetLangCode,
                onSourceClick = { sourceLangCode = it },
                onTargetClick = { targetLangCode = it },
                onSwap = {
                    if (sourceLangCode == "auto") {
                        sourceLangCode = targetLangCode; targetLangCode = "en"
                    } else {
                        val temp = sourceLangCode; sourceLangCode = targetLangCode; targetLangCode = temp
                    }
                    if (resultText.isNotBlank()) {
                        inputText = resultText; resultText = ""
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { performTranslate() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                } else {
                    Icon(imageVector = Icons.Default.Translate, contentDescription = "Translate")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            InputArea(
                modifier = Modifier.weight(0.3f),
                inputText = inputText,
                onValueChange = { inputText = it },
                onTranslate = { performTranslate() },
                onClear = { inputText = ""; resultText = "" }
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            ResultArea(
                modifier = Modifier.weight(0.7f),
                resultText = resultText,
                isLoading = isLoading
            )
        }
    }
}

// === 獨立的輸入區元件 ===
@Composable
fun InputArea(
    modifier: Modifier = Modifier,
    inputText: String,
    onValueChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onClear: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TextField(
            value = inputText,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = stringResource(R.string.input_placeholder),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 0.dp, end = 0.dp, top = 16.dp, bottom = 70.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onTranslate() })
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    val clipData = clipboardManager.getText()
                    if (clipData != null) onValueChange(clipData.text)
                    else Toast.makeText(context, context.getString(R.string.clipboard_empty), Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_paste))
            }

            if (inputText.isNotEmpty()) {
                FilledTonalButton(
                    onClick = onClear,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_clear))
                }
            }
        }
    }
}

// === 獨立的結果區元件 ===
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultArea(
    modifier: Modifier = Modifier,
    resultText: String,
    isLoading: Boolean
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = { },
                onLongClick = {
                    if (resultText.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(resultText))
                        Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                    }
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (resultText.isEmpty() && !isLoading) {
                Text(
                    text = stringResource(R.string.translation_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            } else {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// === 頂部語言選擇欄 ===
@Composable
fun LanguageSelectionBar(
    sourceLang: String,
    targetLang: String,
    onSourceClick: (String) -> Unit,
    onTargetClick: (String) -> Unit,
    onSwap: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                LanguagePickerButton(
                    currentLangName = stringResource(SUPPORTED_LANGUAGES[sourceLang] ?: R.string.lang_detect),
                    isSource = true,
                    onLangSelected = onSourceClick
                )
            }

            IconButton(onClick = onSwap) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = "Swap Languages",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                LanguagePickerButton(
                    currentLangName = stringResource(SUPPORTED_LANGUAGES[targetLang] ?: R.string.lang_zh_tw),
                    isSource = false,
                    onLangSelected = onTargetClick
                )
            }
        }
    }
}

@Composable
fun LanguagePickerButton(
    currentLangName: String,
    isSource: Boolean,
    onLangSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        ElevatedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(5.dp).fillMaxWidth()
        ) {
            Text(
                text = currentLangName,
                modifier = Modifier
                    .basicMarquee()
                    .padding(vertical = 7.dp, horizontal = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            SUPPORTED_LANGUAGES.forEach { (code, nameResId) ->
                if (!isSource && code == "auto") return@forEach
                DropdownMenuItem(
                    text = { Text(stringResource(nameResId)) },
                    onClick = { onLangSelected(code); expanded = false }
                )
            }
        }
    }
}