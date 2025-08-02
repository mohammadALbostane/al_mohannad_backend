package com.example.al_mohannad_backend.controller

import com.example.al_mohannad_backend.model.Product
import com.example.al_mohannad_backend.model.ProductDto
import com.example.al_mohannad_backend.repository.CategoryRepository
import com.example.al_mohannad_backend.repository.ProductRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = ["*"])
class ProductController(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {

    @GetMapping
    fun getAllProducts(@RequestHeader("Host") host: String): List<ProductDto> {
        val baseUrl = "http://$host"

        return productRepository.findAll().map {
            ProductDto(
                id = it.id,
                name = it.name,
                price = it.price,
                imageUrl = "$baseUrl${it.imageUrl}",
                categoryId = it.category?.id ?: 0,
                categoryName = it.category?.name ?: "غير معروف"
            )
        }
    }


    @GetMapping("/{id}")
    fun getProductById(
        @PathVariable id: Long,
        @RequestHeader("Host") host: String
    ): ResponseEntity<ProductDto> {
        val baseUrl = "http://$host"
        val product = productRepository.findById(id)
        return if (product.isPresent)
            ResponseEntity.ok(product.get().toDto(baseUrl))
        else
            ResponseEntity.notFound().build()
    }

    @GetMapping("/category/{categoryId}")
    fun getProductsByCategory(
        @PathVariable categoryId: Long,
        @RequestHeader("Host") host: String
    ): ResponseEntity<List<ProductDto>> {
        println("✅ تم الوصول إلى /ببببببب من العميل")
        val baseUrl = "http://$host"
        val products = productRepository.findByCategoryId(categoryId)
        return if (products.isNotEmpty())
            ResponseEntity.ok(products.map { it.toDto(baseUrl) })
        else
            ResponseEntity.noContent().build()
    }


    @PostMapping("/with-image", consumes = ["multipart/form-data"])
    fun createProductWithImage(
        @RequestParam("name") name: String,
        @RequestParam("price") price: Double,
        @RequestParam("categoryId") categoryId: Long,
        @RequestParam("image") image: MultipartFile,
        @RequestHeader("Host") host: String
    ): ResponseEntity<ProductDto> {
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { RuntimeException("Category not found") }

        val extension = image.originalFilename?.substringAfterLast('.', "") ?: "jpg"
        val fileName = "${UUID.randomUUID()}.$extension"

        val uploadDir = Paths.get("uploads/images")
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir)

        val filePath = uploadDir.resolve(fileName)
        Files.copy(image.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

        val imageUrl = "/uploads/images/$fileName"

        val product = Product(
            name = name,
            price = price,
            imageUrl = imageUrl,
            category = category
        )

        val saved = productRepository.save(product)
        val baseUrl = "http://$host"
        return ResponseEntity.ok(saved.toDto(baseUrl))
    }




    @PutMapping("/{id}", consumes = ["multipart/form-data"])
    fun updateProductWithImage(
        @PathVariable id: Long,
        @RequestParam("name") name: String,
        @RequestParam("price") price: Double,
        @RequestParam("categoryId") categoryId: Long,
        @RequestParam(value = "image", required = false) image: MultipartFile?
    ): ResponseEntity<Product> {
        val productOpt = productRepository.findById(id)
        if (!productOpt.isPresent) return ResponseEntity.notFound().build()

        val category = categoryRepository.findById(categoryId).orElse(null)
            ?: return ResponseEntity.badRequest().build()

        var imageUrl = productOpt.get().imageUrl

        // إذا تم رفع صورة جديدة
        if (image != null && image.originalFilename != null) {
            val extension = image.originalFilename!!.substringAfterLast('.', "")
            val newFileName = UUID.randomUUID().toString() + "." + extension
            val uploadDir = Paths.get("uploads/images")
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir)

            val filePath = uploadDir.resolve(newFileName)
            Files.copy(image.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

            // حذف الصورة القديمة
            val oldPath = Paths.get("uploads${imageUrl}")
            try {
                Files.deleteIfExists(oldPath)
            } catch (_: Exception) {}

            imageUrl = "/uploads/images/$newFileName"
        }

        val updated = productOpt.get().copy(
            name = name,
            price = price,
            imageUrl = imageUrl,
            category = category
        )

        return ResponseEntity.ok(productRepository.save(updated))
    }


    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        val productOpt = productRepository.findById(id)
        if (!productOpt.isPresent) return ResponseEntity.notFound().build()

        val product = productOpt.get()

        // حذف الصورة من المجلد
        try {
            val imagePath = Paths.get("uploads${product.imageUrl}")
            Files.deleteIfExists(imagePath)
        } catch (_: Exception) {}

        productRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

}
fun com.example.al_mohannad_backend.model.Product.toDto(baseUrl: String): ProductDto {
    return ProductDto(
        id = this.id,
        name = this.name,
        price = this.price,
        imageUrl = "$baseUrl${this.imageUrl}",
        categoryId = this.category?.id ?: 0,
        categoryName = this.category?.name ?: "غير معروف"
    )
}

