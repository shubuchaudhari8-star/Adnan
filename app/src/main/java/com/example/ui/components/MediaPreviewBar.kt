package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AttachedMedia
import com.example.data.model.MediaType
import com.example.ui.theme.EmpathyRose
import com.example.ui.theme.NeuralViolet
import com.example.ui.theme.PrivacyEmerald

@Composable
fun MediaPreviewBar(
    attachedMedia: AttachedMedia?,
    onRemoveMedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = attachedMedia != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        if (attachedMedia == null) return@AnimatedVisibility

        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("media_preview_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Media Thumbnail
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        when (attachedMedia.mediaType) {
                            MediaType.IMAGE -> {
                                val context = LocalContext.current
                                val imageModel = remember(attachedMedia.uri, attachedMedia.base64Data) {
                                    if (attachedMedia.uri.startsWith("content://") || attachedMedia.uri.startsWith("file://")) {
                                        Uri.parse(attachedMedia.uri)
                                    } else if (!attachedMedia.base64Data.isNullOrBlank()) {
                                        try {
                                            android.util.Base64.decode(attachedMedia.base64Data, android.util.Base64.DEFAULT)
                                        } catch (e: Exception) {
                                            attachedMedia.uri
                                        }
                                    } else {
                                        attachedMedia.uri
                                    }
                                }
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imageModel)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = attachedMedia.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            MediaType.VIDEO -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(NeuralViolet.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Videocam,
                                        contentDescription = "Video Attachment",
                                        tint = NeuralViolet,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            MediaType.AUDIO -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(EmpathyRose.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Audio Attachment",
                                        tint = EmpathyRose,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = attachedMedia.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PrivacyEmerald.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Ready to Analyze",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrivacyEmerald,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = when (attachedMedia.mediaType) {
                                MediaType.IMAGE -> "ছবি পাঠানোর সাথে সাথে আবেগ ও অনুভূতি বিশ্লেষণ করা হবে"
                                MediaType.VIDEO -> "ভিডিওর দৃশ্যপট ও মানসিক সংকেত বিশ্লেষণ করা হবে"
                                MediaType.AUDIO -> "অডিও কণ্ঠের স্বর ও আবেগ বিশ্লেষণ করা হবে"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onRemoveMedia,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("remove_media_button")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove Media",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
