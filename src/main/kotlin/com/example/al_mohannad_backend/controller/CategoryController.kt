package com.example.al_mohannad_backend.controller

import com.example.al_mohannad_backend.model.Category
import com.example.al_mohannad_backend.model.CategoryDto
import com.example.al_mohannad_backend.repository.CategoryRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = ["*"])
class CategoryController(
    private val categoryRepository: CategoryRepository
) {

    @GetMapping
    fun getAll(@RequestHeader(value = "Host") host: String): List<CategoryDto> {
        println("✅ تم الوصول إلى /ببببببب من العميل")
        val baseUrl = "http://$host"
        return categoryRepository.findAll().map {
            CategoryDto(
                id = it.id,
                name = it.name,
                imageUrl = "$baseUrl${it.imageUrl}"
            )
        }
    }

    @PostMapping(consumes = ["multipart/form-data"])
    fun createWithImage(
        @RequestParam("name") name: String,
        @RequestParam("image") image: MultipartFile
    ): ResponseEntity<Category> {
        return try {
            val originalFilename = image.originalFilename ?: return ResponseEntity.badRequest().build()
            val extension = originalFilename.substringAfterLast('.', "")
            val newFileName = UUID.randomUUID().toString() + "." + extension

            // إنشاء المجلد إذا غير موجود
            val uploadDir = Paths.get("uploads/categories")
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir)

            // حفظ الصورة
            val filePath = uploadDir.resolve(newFileName)
            Files.copy(image.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

            val imageUrl = "/uploads/categories/$newFileName"

            val category = Category(name = name, imageUrl = imageUrl)
            ResponseEntity.ok(categoryRepository.save(category))

        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }
}
