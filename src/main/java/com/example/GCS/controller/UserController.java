package com.example.GCS.controller;


import com.example.GCS.model.User;
import com.example.GCS.service.UserService;
import com.example.GCS.utils.ResponseBuilder;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// 概要:user操作に関わるコントローラー
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 概要: ログイン後のユーザ情報表示の為の値を返す
    @PostMapping("/info")
    public ResponseEntity<Map<String,Object>> editUserInfo(@RequestHeader("Authorization") String idToken,
                                                           @RequestBody Map<String, String> requestBody)
    {
        logger.debug("★idToken is " + idToken);
        logger.debug("★requestBody is " + requestBody);

        // JWT検証用
        FirebaseToken firebaseToken;
        // 返答用
        Map<String, Object> response = new HashMap<>();

        /* ------------------------------
         * JWT検証
         -------------------------------- */
        try {
            firebaseToken = userService.verifyJWT(idToken);
        }catch (IllegalArgumentException e){
            // JWT 引数エラー
            logger.error("Invalid input: " + e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RuntimeException e) {
            // JWT 検証エラー
            logger.error("Token verification failed: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        /* ------------------------------
         * JWTのuidとRequestBodyのuid比較
         -------------------------------- */
        if(!userService.ComparisonOfUID(firebaseToken,requestBody))
        {
            response.put("success", false);
            response.put("message", "uidが一致しません");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /* ------------------------------
         * DBから値取得
         -------------------------------- */
        User user =  userService.getPersonalInfomation(firebaseToken.getUid());
        if(user == null)
        {
            response.put("success", false);
            response.put("message", "DBに登録されている値と不一致");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        logger.debug("★user is "+ user);
        /* ------------------------------
         * レスポンスの組み立て
         -------------------------------- */
        response.put("success", true);
        response.put("uid",user.getGoogleId());
        response.put("notificationEmail",user.getNotificationEmail());
        response.put("gitName",user.getGitName());
        response.put("notificationTime",user.getTime());
        logger.debug("★response is" + response);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
