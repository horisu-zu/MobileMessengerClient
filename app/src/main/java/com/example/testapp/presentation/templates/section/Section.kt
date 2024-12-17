package com.example.testapp.presentation.templates.section

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun Section(
    modifier: Modifier = Modifier,
    title: String? = null,
    items: List<SectionItem>,
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val (titleRef, contentRef) = createRefs()

        if (title != null) {
            SectionTitle(
                title = title,
                modifier = Modifier
                    .constrainAs(titleRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .constrainAs(contentRef) {
                    top.linkTo(titleRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            items.forEachIndexed { index, item ->
                when (item) {
                    is SectionItem.Text -> TextItem(
                        title = item.title,
                        subtitle = item.subtitle,
                        onClick = item.onClick,
                        showDivider = index < items.size - 1
                    )
                    is SectionItem.Icon -> IconItem(
                        title = item.title,
                        icon = item.icon,
                        onClick = item.onClick,
                        trailingText = item.trailingText,
                        showDivider = index < items.size - 1
                    )
                    is SectionItem.Radio -> RadioItem(
                        title = item.title,
                        selected = item.selected,
                        onClick = item.onClick,
                        showDivider = index < items.size - 1
                    )
                    is SectionItem.Input -> InputItem(
                        label = item.label,
                        value = item.value,
                        onValueChange = item.onValueChange,
                        limit = item.limit,
                        placeholder = item.placeholder,
                        showDivider = index < items.size - 1,
                        inputFilter = item.inputFilter
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun TextItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp)
    ) {
        val (titleRef, subtitleRef, divider) = createRefs()

        Text(
            text = title,
            modifier = Modifier.constrainAs(titleRef) {
                top.linkTo(parent.top, margin = 4.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            modifier = Modifier.constrainAs(subtitleRef) {
                top.linkTo(titleRef.bottom)
                bottom.linkTo(parent.bottom, margin = 4.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if(showDivider) {
            HorizontalDivider(
                modifier = Modifier.constrainAs(divider) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun IconItem(
    title: String,
    icon: Painter,
    onClick: () -> Unit,
    trailingText: String? = null,
    showDivider: Boolean = true
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp)
    ) {
        val (titleRef, iconRef, trailingRef, divider) = createRefs()

        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier
                .constrainAs(iconRef) {
                    top.linkTo(parent.top, margin = 12.dp)
                    bottom.linkTo(parent.bottom, margin = 12.dp)
                    start.linkTo(parent.start)
                }
                .size(24.dp)
        )

        Text(
            text = title,
            modifier = Modifier.constrainAs(titleRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(iconRef.end, margin = 16.dp)
                width = Dimension.fillToConstraints
            },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        trailingText?.let {
            Card(
                modifier = Modifier.constrainAs(trailingRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 16.dp)
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            ) {
                Text(
                    text = trailingText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if(showDivider) {
            HorizontalDivider(
                modifier = Modifier.constrainAs(divider) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(titleRef.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun RadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp)
    ) {
        val (titleRef, radioButtonRef, divider) = createRefs()

        Text(
            text = title,
            modifier = Modifier.constrainAs(titleRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(radioButtonRef.end, margin = 16.dp)
            },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        RadioButton(
            selected = selected,
            onClick = null, // Click handles by Constraint
            modifier = Modifier.constrainAs(radioButtonRef) {
                top.linkTo(parent.top, margin = 8.dp)
                bottom.linkTo(parent.bottom, margin = 8.dp)
                start.linkTo(parent.start)
            }
        )

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.constrainAs(divider) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(titleRef.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun InputItem(
    label: String,
    value: String,
    placeholder: String = "",
    limit: Int? = null,
    showDivider: Boolean,
    inputFilter: (String) -> String = { it },
    onValueChange: (String) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
    ) {
        val (labelRef, inputFieldRef, divider) = createRefs()

        Text(
            text = label,
            modifier = Modifier.constrainAs(labelRef) {
                top.linkTo(parent.top, margin = 4.dp)
                start.linkTo(parent.start)
                bottom.linkTo(inputFieldRef.top, margin = 4.dp)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )

        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                val filteredValue = inputFilter(newValue)
                if (limit == null || filteredValue.length <= limit) {
                    onValueChange(filteredValue)
                }
            },
            modifier = Modifier
                .constrainAs(inputFieldRef) {
                    top.linkTo(labelRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .background(Color.Transparent)
                .padding(vertical = 8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            },
        )

        if(showDivider) {
            HorizontalDivider(
                modifier = Modifier.constrainAs(divider) {
                    top.linkTo(inputFieldRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}