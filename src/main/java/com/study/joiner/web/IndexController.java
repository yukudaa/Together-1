package com.study.joiner.web;

import com.mysql.cj.Session;
import com.study.joiner.config.auth.LoginUser;
import com.study.joiner.config.auth.dto.SessionUser;
import com.study.joiner.domain.user.Board;
import com.study.joiner.domain.user.SocialUser;
import com.study.joiner.repository.UserRepository;
import com.study.joiner.service.BoardService;
import com.study.joiner.service.UserService;
import com.study.joiner.web.dto.BoardDto;
import com.study.joiner.web.dto.UserResponseDto;
import com.study.joiner.web.dto.UserUpdateRequestDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class IndexController {
    private final UserService userService;
    private final BoardService boardService;
    private final UserRepository userRepository;

    // 메인페이지 -- 완
    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) {
        List<BoardDto> boardDtoList = boardService.getBoardList();
        model.addAttribute("boardList", boardDtoList);
        if(user != null) {
            model.addAttribute("user", user);
        }
        return "index";
    }

    // 내 작성 글 목록 조회 (Pageable 보류)
    @GetMapping("/user/board")
    public String userBoardList(@LoginUser SessionUser user, Model model) {
        SocialUser socialUser = userRepository.findByEmailFetchBL(user.getEmail()).orElseThrow();
        List<BoardDto> boardDtos = boardService.getUserBoardList(socialUser);
        model.addAttribute("userBoardList", boardDtos);
        return "view/userBoard";
    }
//
//    // 내 작성 글 목록 조회 (Pageable 적용 로직)
//    @GetMapping("/user/board")
//    public String userBoardList(@LoginUser SessionUser user, Pageable pageable, Model model) {
//        SocialUser socialUser = userRepository.findByEmailFetchBL(user.getEmail()).orElseThrow();
//        PageImpl<BoardDto> boardDtos = boardService.getUserBoardList(socialUser, pageable);
//        model.addAttribute("userBoardList", boardDtos);
//        return "view/userBoard";
//    }

    // 내 작성 글 조회 -- 완
     @GetMapping("/user/board/{id}")
     public String userBoard(@PathVariable Long id, @LoginUser SessionUser user, Model model) {
        SocialUser socialUser = userRepository.findByEmailFetchBL(user.getEmail()).orElseThrow();
        BoardDto boardDto = boardService.getUserBoard(id, socialUser);
        model.addAttribute("userBoard", boardDto);
        return "view/userBoard";
    }

    // 내 정보 수정 조회 -- 완
    @GetMapping("/user/update")
    public String userUpdate(@LoginUser SessionUser user, Model model) {
        UserResponseDto userResponseDto = userService.getInfo(user.getEmail());
        model.addAttribute("user", userResponseDto);
        return "view/setting";
    }

    // 내 정보 수정 처리 -- 완
    @PostMapping("/user/update")
    public String userUpdateComplete(@LoginUser SessionUser user, @ModelAttribute UserUpdateRequestDto requestDto, BindingResult bindingResult, Model model) {

        // 검증
        if(bindingResult.hasErrors()) {
            // 회원가입 실패 시 입력 데이터 값 유지
            model.addAttribute("userResponseDto",requestDto);

            // 유효성 검사를 통과하지 못 한 필드와 메시지 핸들링
            Map<String, String > errorMap = new HashMap<>();

            for(FieldError error : bindingResult.getFieldErrors()) {
                errorMap.put("valid_"+ error.getField(), error.getDefaultMessage());
            }
            return "redirect:/";
        }

        UserResponseDto userResponseDto = userService.update(user.getEmail(), requestDto);
        model.addAttribute("user", userResponseDto);
        return "view/setting";
    }



   // 회원 탈퇴 -- 완
    @DeleteMapping("/user/delete")
    public String userDelete(@LoginUser SessionUser user) {
        SocialUser socialUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new IllegalArgumentException());
        userService.delete(socialUser);
        return "redirect:/";
    }
}
