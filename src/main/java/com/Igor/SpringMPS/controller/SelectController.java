package com.Igor.SpringMPS.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import com.Igor.SpringMPS.ListTransformerSubst;
import com.Igor.SpringMPS.TempTransformerSubst;
import com.Igor.SpringMPS.data.TransformerRepository;
import com.Igor.SpringMPS.entities.TransformerSubst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;

import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/select")
//@SessionAttributes(types = TransformerSubst.class)
//@SessionAttributes(value = "currentSubst")
@ConfigurationProperties(prefix="mps.substations")
public class SelectController {

    private int pageSize = 10;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @ModelAttribute("currentSubst")
    public TransformerSubst createSubst(){
        return new TransformerSubst();
    }

    private TransformerRepository transformerRepo;
    @Autowired
    public SelectController (TransformerRepository transformerRepo){
        this.transformerRepo = transformerRepo;
    }

    @GetMapping
    //public String showSelectForm(@ModelAttribute("currentSubst") TransformerSubst tp, Model model,
    public String showSelectForm( TransformerSubst tp, Model model,
                                 @RequestParam("pageNumber") Optional<Integer> pageNumber,
                                  @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
                                  @AuthenticationPrincipal OAuth2User oauth2User){

        model.addAttribute("username",oauth2User.getAttribute("name"));
        model.addAttribute("usermail",oauth2User.getAttribute("email"));
        model.addAttribute("userauthorities",oauth2User.getAuthorities());


        int currentPage = pageNumber.orElse(0);

        List<TransformerSubst> substs = new ArrayList<>();
        Pageable pageable =  PageRequest.of(currentPage, pageSize);
        Page<TransformerSubst> page = null;
        page = transformerRepo.findAll(pageable);
        substs = page.getContent();
        //transformerRepo.findAll().forEach(i->substs.add(i));
        model.addAttribute("listsubst",substs);

        int prevPage = 0;
        int nextPage = 0;
        int totalPages = page.getTotalPages();
        if (currentPage > 0)
            prevPage = currentPage - 1;
        if (totalPages - 1 > currentPage)
            nextPage = currentPage + 1;
        else
            nextPage = currentPage;
        model.addAttribute("prevpage", prevPage);
        model.addAttribute("nextpage", nextPage);
        model.addAttribute("currentpage", currentPage + 1);
        model.addAttribute("totalpages",totalPages);

        long totalSubst = page.getTotalElements();
        model.addAttribute("totalSubst",totalSubst);

        //model.addAttribute("select",new BaseTransformerSubst());
        return "select";
    }
    @PostMapping(params = "edit")
    //public String processSelect( @ModelAttribute("currentSubst")  TransformerSubst tp,TempTransformerSubst tempTp){
    //public String processSelect( @ModelAttribute TransformerSubst tempTp){
    //public String processSelect(@ModelAttribute("currentSubst") TransformerSubst tempTp, Errors errors, Model model, HttpSession httpSession, RedirectAttributes attributes){
    public String processSelect(@ModelAttribute("currentSubst") TransformerSubst tempTp, Errors errors, Model model, HttpSession httpSession,HttpServletRequest request){
        if(tempTp.getId()==null)
            return "redirect:/select";
        log.info("Processing select: " + tempTp.toString());
        model.addAttribute("currentSubst",tempTp);
        //httpSession.setAttribute("currentSubst",tempTp);
        model.addAttribute("id", tempTp.getId());

        return "redirect:/edit/current";
    }
    @PostMapping(params="delete")
    public String deleteForm(TransformerSubst transformerSubst){
        if(transformerSubst.getId()==null)
            return "redirect:/select";
        transformerRepo.deleteById(transformerSubst.getId());
        return "successful";
    }
}
