package com.example

import com.example.engine.MarksDistributionEngine
import com.example.model.Difficulty
import com.example.model.QuestionType
import com.example.security.KeystoreCryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `verify deterministic marks distribution engine for 40 marks`() {
    val blueprint = MarksDistributionEngine.generateBlueprint(40)
    val totalBlueprintMarks = blueprint.sectionBlueprints.sumOf { it.totalMarks }
    assertEquals(40, totalBlueprintMarks)
  }

  @Test
  fun `verify deterministic marks distribution engine for 50 marks`() {
    val blueprint = MarksDistributionEngine.generateBlueprint(50)
    val totalBlueprintMarks = blueprint.sectionBlueprints.sumOf { it.totalMarks }
    assertEquals(50, totalBlueprintMarks)
  }

  @Test
  fun `verify deterministic marks distribution engine for 60 marks`() {
    val blueprint = MarksDistributionEngine.generateBlueprint(60)
    val totalBlueprintMarks = blueprint.sectionBlueprints.sumOf { it.totalMarks }
    assertEquals(60, totalBlueprintMarks)
  }

  @Test
  fun `verify deterministic marks distribution engine for 70 marks`() {
    val blueprint = MarksDistributionEngine.generateBlueprint(70)
    val totalBlueprintMarks = blueprint.sectionBlueprints.sumOf { it.totalMarks }
    assertEquals(70, totalBlueprintMarks)
  }

  @Test
  fun `verify deterministic marks distribution engine for 80 marks`() {
    val blueprint = MarksDistributionEngine.generateBlueprint(80)
    val totalBlueprintMarks = blueprint.sectionBlueprints.sumOf { it.totalMarks }
    assertEquals(80, totalBlueprintMarks)
  }

  @Test
  fun `verify deterministic marks distribution engine for 90 marks`() {
    val blueprint = MarksDistributionEngine.generateBlueprint(90)
    val totalBlueprintMarks = blueprint.sectionBlueprints.sumOf { it.totalMarks }
    assertEquals(90, totalBlueprintMarks)
  }

  @Test
  fun `verify deterministic marks distribution engine for 100 marks`() {
    val blueprint = MarksDistributionEngine.generateBlueprint(100)
    val totalBlueprintMarks = blueprint.sectionBlueprints.sumOf { it.totalMarks }
    assertEquals(100, totalBlueprintMarks)
  }

  @Test
  fun `verify new question types are supported`() {
    assertTrue(QuestionType.entries.contains(QuestionType.ASSERTION_REASON))
    assertTrue(QuestionType.entries.contains(QuestionType.NUMERICAL))
    assertTrue(QuestionType.entries.contains(QuestionType.DIAGRAM))
  }

  @Test
  fun `verify mixed difficulty is present in Difficulty enum`() {
    assertTrue(Difficulty.entries.contains(Difficulty.MIXED))
    assertTrue(Difficulty.entries.contains(Difficulty.EASY))
    assertTrue(Difficulty.entries.contains(Difficulty.MODERATE))
    assertTrue(Difficulty.entries.contains(Difficulty.DIFFICULT))
  }

  @Test
  fun `verify api key masking hides sensitive token characters`() {
    val rawKey = "gsk_test1234567890abcdefghijklmnopqrstuvwxyz"
    val masked = KeystoreCryptoManager.maskApiKey(rawKey)
    assertTrue(masked.startsWith("gsk_"))
    assertTrue(masked.endsWith("wxyz"))
    assertTrue(masked.contains("•"))
    assertFalse(masked.contains("1234567890"))
  }

  @Test
  fun `verify encryption and decryption roundtrip`() {
    val secretKey = "gsk_live_super_secret_token_12345"
    val encrypted = KeystoreCryptoManager.encrypt(secretKey)
    assertNotNull(encrypted)
    assertTrue(encrypted.isNotBlank())
    val decrypted = KeystoreCryptoManager.decrypt(encrypted)
    assertEquals(secretKey, decrypted)
  }
}
