package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.PaperGeniusViewModel
import com.example.ui.components.PaperGeniusFooter
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PurpleAccent

@Composable
fun FeaturesScreen(
    viewModel: PaperGeniusViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEEF2FF)
            ) {
                Text(
                    text = "CORE CAPABILITIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "How PaperGenius AI Works",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Combining vision-capable AI models with a deterministic balancing allocation algorithm to generate realistic Indian school examination papers.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF64748B),
                    lineHeight = 20.sp
                )
            )
        }

        // Capability Card 1: Multimodal OCR
        CapabilityCard(
            icon = Icons.Default.CameraAlt,
            iconTint = IndigoPrimary,
            iconBg = Color(0xFFEEF2FF),
            title = "Multimodal OCR & Vision Understanding",
            description = "Our vision pipeline inspects textbook photographs taken on Android and iOS mobile devices. It reads complex mathematical notation, balanced chemical equations (e.g. redox, precipitates), circuit diagrams, and biological definitions.",
            bullet1 = "Detects blurry or unreadable sections and warns before generation",
            bullet2 = "Synthesizes original conceptual questions rather than copy-pasting"
        )

        // Capability Card 2: Strict Marks Distribution
        CapabilityCard(
            icon = Icons.Default.Psychology,
            iconTint = PurpleAccent,
            iconBg = Color(0xFFF3E8FF),
            title = "Strict Marks Distribution Engine",
            description = "Unlike generic AI chat models that frequently produce papers with arithmetic errors, PaperGenius AI enforces a deterministic marks distribution blueprint.",
            bullet1 = "Strictly guarantees: Sum of all question marks = Selected Total (40 to 100)",
            bullet2 = "Automatic section breakdown (MCQ, VSA, Short Answer, Long Answer, Case-based)"
        )

        // Capability Card 3: Separated Teacher Answer Keys
        CapabilityCard(
            icon = Icons.Default.Description,
            iconTint = EmeraldSuccess,
            iconBg = Color(0xFFECFDF5),
            title = "Separated Teacher Answer Keys & Solutions",
            description = "By default, questions and answers are completely segregated. Students receive a clean, unblemished exam sheet, while evaluators receive step-by-step marking schemes.",
            bullet1 = "Step Marking: 1 mark for formula, 1 mark for substitution, 1 mark for final unit",
            bullet2 = "Instant download of both Question Paper PDF and Answer Key PDF"
        )

        // Launch Generator Button
        Button(
            onClick = { viewModel.setScreen(AppScreen.GENERATOR) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("features_launch_generator_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Launch Generator Now",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Professional Footer (NO APK references)
        PaperGeniusFooter(onNavigate = { viewModel.setScreen(it) })
    }
}

@Composable
private fun CapabilityCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    description: String,
    bullet1: String,
    bullet2: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletRow(bullet1)
                BulletRow(bullet2)
            }
        }
    }
}

@Composable
private fun BulletRow(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldSuccess,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = NavyDark,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
