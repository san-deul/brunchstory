package com.dosirak.prj.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dosirak.prj.dto.UserDto;
import com.dosirak.prj.service.UserService;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;

@RequestMapping("/user")
@RequiredArgsConstructor
@Controller
public class UserController {

  private final UserService userService;
  
  @GetMapping("/signup.page")
  public String signupPage() {
    return "user/signup";
  }
  
  @PostMapping(value="/checkEmail.do", produces="application/json")
  public ResponseEntity<Map<String, Object>> checkEmail(@RequestBody Map<String, Object> params) {
    return userService.checkEmail(params);
  }
  
  @PostMapping(value="/sendCode.do", produces="application/json")
  public ResponseEntity<Map<String, Object>> sendCode(@RequestBody Map<String, Object> params) {
    return userService.sendCode(params);
  }
  
  @PostMapping("/signup.do")
  public String signup(HttpServletRequest request, HttpServletResponse response) {
    userService.signup(request, response);
    return "redirect:/main.page";
  }
  
  @GetMapping("/leave.do")
  public void leave(HttpServletRequest request, HttpServletResponse response) {
    userService.leave(request, response);
  }
  
  @GetMapping("/login.page")
  public String loginPage(HttpServletRequest request
  											, Model model) {
  	
  	// Log In 페이지로 url 넘겨 주기 (로그인 후 이동할 경로를 의미함)
  	model.addAttribute("url", userService.getRedirectURLAfterLogin(request));
  	  	
  	// Log In 페이지로 naverLoginURL 넘겨 주기 (네이버 로그인 요청 주소를 의미함)
  	model.addAttribute("naverLoginURL", userService.getNaverLoginURL(request));
  	
  	return "user/login";
  }
  
  @PostMapping("/login.do")
  public void login(HttpServletRequest request, HttpServletResponse response) {
    userService.login(request, response);
  }
  
  @GetMapping("/naver/getAccessToken.do")
  public String getAccessToken(HttpServletRequest request) {
    String accessToken = userService.getNaverLoginAccessToken(request);
    return "redirect:/user/naver/getProfile.do?accessToken=" + accessToken;
  }
  
  @GetMapping("/naver/getProfile.do")
  public String getProfile(HttpServletRequest request, Model model) {
    
    // 네이버로부터 받은 프로필 정보
    UserDto naverUser = userService.getNaverLoginProfile(request.getParameter("accessToken"));
    
    // 반환 경로
    String path = null;
    
    // 프로필이 DB에 있는지 확인 (있으면 Sign In, 없으면 Sign Up)
    if(userService.hasUser(naverUser)) {
      // Sign In
      userService.naverSignin(request, naverUser);
      path = "redirect:/main.page";
    } else {
      // Sign Up (네이버 가입 화면으로 이동)
      model.addAttribute("naverUser", naverUser);
      path = "user/naver_signup";
    }
    
    return path;
    
  }
 
  @GetMapping("/logout.do")
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    userService.logout(request, response);
  }
  
  // SD 코드
  //mypage로 넘어가는거임ㅅ시
  
  @GetMapping("/mypage.do")
  public String myPage(@RequestParam(value="userNo", required=false, defaultValue="2") int userNo,
                                    Model model) {
    userService.loadUserByNo(userNo);
    return "user/mypage";
  }
  
  // ★★★ 불러오기

  
 // profile jsp 로 user정보 담아서 이동
  //userNo 에 강제로 숫자 넣어서 이동함
  
  
 
  @GetMapping("/profile.do")
  public String modifyProfile(@RequestParam("userNo") int userNo, Model model) {
      // 사용자 번호를 기반으로 사용자 정보를 가져옴
      UserDto user = userService.loadUserByNo(userNo);

      // 사용자의 닉네임 사용하기
      String nickname = user.getNickname();
      String blogContents = user.getBlogContents();
      String blogImgPath = user.getBlogImgPath();

      // 모델에 사용자 정보를 추가하여 프로필 페이지로 전달
      //model.addAttribute("userNo", user.getUserNo());
      model.addAttribute("nickname", nickname);
      model.addAttribute("blogContents", blogContents);
      model.addAttribute("blogImgPath",blogImgPath);

      // 다른 필요한 정보도 모델에 추가할 수 있음

      System.out.println(nickname);
      System.out.println(blogImgPath);
     // redirectAttributes.addFlashAttribute("modifyResult", modifyCount == 1 ? "1" : "0");
      // 프로필 페이지로 이동
      return "user/profile";
  }
  
  
  
  @PostMapping("/modify.do")
  public String modifyProfile(@RequestParam(value = "blogImgPath", required = false) MultipartFile blogImgPath,
                              @RequestParam("userNo") int userNo,
                              @RequestParam("nickname") String nickname,
                              @RequestParam("blogContents") String blogContents,
//                              RedirectAttributes redirectAttributes) {
                              Model model) {
      int modifyCount = userService.modifyProfile(userNo, nickname, blogContents, blogImgPath);
//      redirectAttributes.addFlashAttribute("modifyResult", modifyCount == 1 ? "1" : "0");
      // 수정 결과에 따라 리다이렉트할 주소와 함께 리턴
     // System.out.println("수정:"+modifyCount);
      model.addAttribute("msg", "프로필이 수정되었습니다.");
      //model.addAttribute("url", "/user/mypage.do");
      model.addAttribute("url", "./mypage.do");
      //return "alert";
      return "/user/modifyalert";
  }
  
  
}

   
  

