package com.example.al_mohannad_backend.repository


import com.example.al_mohannad_backend.model.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long>