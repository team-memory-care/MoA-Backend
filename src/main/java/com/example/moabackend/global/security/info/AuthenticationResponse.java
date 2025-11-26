package com.example.moabackend.global.security.info;

import com.example.moabackend.global.code.ErrorCode;
import com.example.moabackend.global.code.GlobalSuccessCode;
import com.example.moabackend.global.code.SuccessCode;
import com.example.moabackend.global.constant.Constants;
import com.example.moabackend.global.security.dto.JwtDTO;
import com.example.moabackend.global.security.utils.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationResponse {

    public static void makeLoginSuccessResponse(
            HttpServletResponse response,
            String domain,
            JwtDTO jwtDto,
            Long refreshExpiration
    ) throws IOException {

        CookieUtil.addSecureCookie(
                response,
                domain,
                Constants.REFRESH_COOKIE_NAME,
                jwtDto.refreshToken(),
                refreshExpiration
        );

        makeSuccessResponse(response);
    }

    public static void makeSuccessResponse(HttpServletResponse response) throws IOException {

        SuccessCode successCode = GlobalSuccessCode.SUCCESS;
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(successCode.getStatus().value());


        Map<String, Object> data = new HashMap<>();

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "요청에 대해 정상적으로 처리되었습니다.");
        body.put("data", data);

        response.getWriter().write(JSONValue.toJSONString(body));
    }

    public static void makeFailureResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getStatus().value());

        Map<String, Object> body= new HashMap<>();
        body.put("success", false);
        body.put("message", "요청이 실패했습니다.");
        body.put("data", null);

        response.getWriter().write(JSONValue.toJSONString(body));
    }
}
