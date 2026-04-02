package com.pec.filippov.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pec.filippov.data.Student
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun ProfileScreen(
    student: Student,
    onBack: () -> Unit,
    onChangeAvatar: () -> Unit
) {
    val primaryBlue = Color(0xFF6797FF)
    val lightBlue = Color(0xFF8BAAFF) // Lighter shade for the emerging card effect

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBlue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 12.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, 
                        contentDescription = "Back", 
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Button(
                    onClick = onChangeAvatar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33000000)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "изменить", 
                        color = Color.White, 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Circle
                val avatarBitmap = remember(student.avatarBase64) {
                    decodeBase64ToBitmap(student.avatarBase64)
                }

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap.asImageBitmap(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder or default icon could go here
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = student.fullName,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 30.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = student.issueDate, 
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Information Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 0.dp), // Can be adjusted for emerging effect
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(containerColor = lightBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        InfoRow(label = "группа", value = student.course) // Using course as group
                        HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
                        InfoRow(label = "орг", value = student.organization)
                        HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
                        InfoRow(label = "курс", value = "${student.course} курс")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
                        // Adding specialty which was in DB
                        InfoRow(label = "спец", value = student.specialty)
                        HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
                        InfoRow(label = "срок обуч.", value = student.issueDate) // issueDate as placeholder
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // QR Code at the bottom
                // Switching from student.id to student.hash as per user request
                val qrBitmap = remember(student.hash, student.id) {
                    generateQRCode(student.hash ?: student.id)
                }

                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .size(160.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

fun decodeBase64ToBitmap(base64Str: String?): Bitmap? {
    if (base64Str == null) return null
    return try {
        val pureBase64 = if (base64Str.startsWith("data:")) {
            base64Str.substringAfter(",")
        } else {
            base64Str
        }
        val bytes = Base64.decode(pureBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}

fun generateQRCode(text: String): Bitmap? {
    val writer = QRCodeWriter()
    return try {
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
