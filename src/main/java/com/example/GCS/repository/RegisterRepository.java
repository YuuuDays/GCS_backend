package com.example.GCS.repository;

import com.example.GCS.dto.RegisterDTO;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RegisterRepository extends MongoRepository<RegisterDTO,Long> {
    // DBにバリデーションチェック済みの値を登録する

}
