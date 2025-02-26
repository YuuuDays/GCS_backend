package com.example.GCS.repository;

import com.example.GCS.dto.RegisterDTO;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisterRepository extends MongoRepository<RegisterDTO,String> {
    // DBにバリデーションチェック済みの値を登録する


}
