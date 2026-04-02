package com.pec.filippov.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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
    val skyBlue = Color(0xFF6797FF)
    val buttonBlue = Color(0xFF4C84FF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(skyBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = buttonBlue),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("изменить", color = Color.White, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Avatar with white border
            val avatarBitmap = remember(student.avatarBase64) {
                decodeBase64ToBitmap(student.avatarBase64)
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(2.dp, Color.White, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
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
                    Text(
                        text = student.fullName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = student.fullName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Text(
                text = student.issueDate,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Info Card Style (Screenshot 4)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    // Small top button looking thing? "узнать больше" or similar?
                    // In scr 2 there's a pill "узнать больше"
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("узнать больше", color = Color.White, fontSize = 14.sp)
                    }

                    InfoRow(label = "группа", value = student.course)
                    InfoRow(label = "орг", value = student.organization)
                    InfoRow(label = "курс", value = "${student.course[0]} курс") // Simplified
                    // Adding other rows from screenshot if data exists
                    InfoRow(label = "ном.тел", value = "+7(901)-788-27-38") // Placeholder from screenshot
                    InfoRow(label = "куратор", value = "Поливанова Е. В.") // Placeholder
                    InfoRow(label = "срок обуч.", value = "01.09.23 — 01.09.27") // Placeholder
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // QR Code
            val qrBitmap = remember(student.hash, student.id) {
                generateQRCode(student.hash ?: student.id)
            }

            if (qrBitmap != null) {
                Surface(
                    modifier = Modifier.size(180.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color.White, thickness = 1.dp)
    }
}

