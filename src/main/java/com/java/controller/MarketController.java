package com.java.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.java.config.SessionConst;
import com.java.dto.GachaPullResultDto;
import com.java.dto.LoginMember;
import com.java.dto.MyItemDto;
import com.java.game.config.GachaConfig;
import com.java.service.GachaService;
import com.java.service.MarketService;
import com.java.photocard.service.PhotoCardService;
import com.java.dto.PhotoCardBatchResultDto;
import com.java.dto.PhotoCardDrawResultDto;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/market")
public class MarketController {

    private final MarketService marketService;
    private final GachaService gachaService;
    private final PhotoCardService photoCardService;

    public MarketController(MarketService marketService, GachaService gachaService,
            PhotoCardService photoCardService) {
        this.marketService = marketService;
        this.gachaService = gachaService;
        this.photoCardService = photoCardService;
    }

    @GetMapping("/shop")
    public String shop(HttpSession session, Model model) {
        LoginMember loginMember =
                (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);

        int coin = 0;
        if (loginMember != null) {
            marketService.ensureMinimumCoin(loginMember.mno(), MarketService.DEFAULT_MIN_COIN);
            coin = marketService.getCurrentCoin(loginMember.mno());
        }

        model.addAttribute("marketTab", "shop");
        model.addAttribute("currentCoin", coin);
        return "market/shop";
    }

    @GetMapping("/gacha")
    public String gacha(@RequestParam(value = "eventId", required = false) Long eventId,
            HttpSession session, Model model) {
        model.addAttribute("marketTab", "gacha");
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberIdForGacha = loginMember != null ? loginMember.mno() : null;
        model.addAttribute("gachaSettings", gachaService.getPublicSettings(eventId, memberIdForGacha));
        model.addAttribute("gachaEventId", eventId);

        if (loginMember != null) {
            Long mno = loginMember.mno();
            marketService.ensureMinimumCoin(mno, MarketService.DEFAULT_MIN_COIN);
            model.addAttribute("currentCoin", marketService.getCurrentCoin(mno));
            model.addAttribute("myTrainees", gachaService.listOwnedTrainees(mno));
        } else {
            model.addAttribute("currentCoin", 0);
            model.addAttribute("myTrainees", List.of());
        }
        return "market/gacha";
    }

    @GetMapping("/photocard")
    public String photocard(HttpSession session, Model model) {
        model.addAttribute("marketTab", "photocard");
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember != null) {
            marketService.ensureMinimumCoin(loginMember.mno(), MarketService.DEFAULT_MIN_COIN);
            model.addAttribute("currentCoin", marketService.getCurrentCoin(loginMember.mno()));
        } else {
            model.addAttribute("currentCoin", 0);
        }
        model.addAttribute("pullCost", PhotoCardService.PULL_COST_COIN);
        model.addAttribute("photoCardPrice5", PhotoCardService.PHOTOCARD_PRICE_5);
        model.addAttribute("photoCardPrice10", PhotoCardService.PHOTOCARD_PRICE_10);
        return "market/photocard";
    }

    @PostMapping("/photocard/pull")
    @ResponseBody
    public Map<String, Object> photocardPull(@RequestBody(required = false) Map<String, Object> body,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            result.put("result", "logout");
            return result;
        }
        int pulls = 1;
        if (body != null && body.get("pulls") instanceof Number) {
            pulls = ((Number) body.get("pulls")).intValue();
        }
        if (pulls == 5 || pulls == 10) {
            PhotoCardBatchResultDto batch = photoCardService.pullBatch(loginMember.mno(), pulls);
            result.put("result", batch.result());
            if (batch.message() != null) {
                result.put("message", batch.message());
            }
            if (batch.currentCoin() != null) {
                result.put("currentCoin", batch.currentCoin());
            }
            if (batch.lines() != null) {
                result.put("lines", batch.lines());
            }
            return result;
        }
        PhotoCardDrawResultDto dto = photoCardService.pull(loginMember.mno());
        result.put("result", dto.result());
        result.put("message", dto.message());
        if (dto.grade() != null) {
            result.put("grade", dto.grade());
        }
        if (dto.displayName() != null) {
            result.put("displayName", dto.displayName());
        }
        if (dto.traineeId() != null) {
            result.put("traineeId", dto.traineeId());
        }
        if (dto.currentCoin() != null) {
            result.put("currentCoin", dto.currentCoin());
        }
        if (dto.imagePath() != null) {
            result.put("imagePath", dto.imagePath());
        }
        if (dto.traineeName() != null) {
            result.put("traineeName", dto.traineeName());
        }
        return result;
    }

    @PostMapping("/gacha/pull")
    @ResponseBody
    public Map<String, Object> gachaPull(@RequestBody Map<String, Object> param, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            result.put("result", "logout");
            return result;
        }
        try {
            int pulls = 1;
            Object p = param != null ? param.get("pulls") : null;
            if (p instanceof Number) {
                pulls = ((Number) p).intValue();
            }
            String poolId = param != null && param.get("poolId") instanceof String
                    ? (String) param.get("poolId")
                    : GachaConfig.DEFAULT_POOL_ID;
            Long eventId = null;
            Object ev = param != null ? param.get("eventId") : null;
            if (ev instanceof Number) {
                eventId = ((Number) ev).longValue();
            }

            GachaPullResultDto dto = gachaService.pull(loginMember.mno(), pulls, poolId, eventId);
            result.put("result", dto.result());
            result.put("currentCoin", dto.currentCoin());
            result.put("pulls", dto.pulls());
            if (dto.message() != null) {
                result.put("message", dto.message());
            }
            if ("success".equals(dto.result())) {
                result.put("myTrainees", gachaService.listOwnedTrainees(loginMember.mno()));
            }
            return result;
        } catch (Exception e) {
            result.put("result", "error");
            result.put("message", "뽑기 처리 중 오류가 발생했습니다.");
            return result;
        }
    }

    @PostMapping("/buyItem")
    @ResponseBody
    public Map<String, Object> buyItem(@RequestBody Map<String, Object> param, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            LoginMember loginMember =
                    (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);

            if (loginMember == null) {
                result.put("result", "logout");
                return result;
            }

            String itemName = (String) param.get("itemName");
            int price = ((Number) param.get("price")).intValue();
            Long memberId = loginMember.mno();
            List<String> boughtItemNames = resolveBoughtItemNames(itemName);

            int currentCoin = marketService.getCurrentCoin(memberId);

            if (currentCoin < price) {
                result.put("result", "lack");
                result.put("currentCoin", currentCoin);
                return result;
            }

            String buyResult = marketService.buyItem(memberId, itemName, price);

            result.put("result", buyResult);
            result.put("currentCoin", marketService.getCurrentCoin(memberId));
            result.put("items", marketService.getMyItems(memberId));
            result.put("boughtItemNames", boughtItemNames);
            return result;

        } catch (Exception e) {
            result.put("result", "error");
            return result;
        }
    }

    @GetMapping("/myItems")
    @ResponseBody
    public List<MyItemDto> myItems(HttpSession session) {
        LoginMember loginMember =
                (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginMember == null) {
            return List.of();
        }

        Long memberId = loginMember.mno();
        return marketService.getMyItems(memberId);
    }

    private List<String> resolveBoughtItemNames(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return List.of();
        }
        if ("올라운드 패키지 박스".equals(itemName)) {
            List<String> items = new ArrayList<>();
            items.add("보컬 워터");
            items.add("댄스 슈즈");
            items.add("팬레터");
            items.add("릴렉스 캔디");
            items.add("팀 스낵 박스");
            return items;
        }
        return List.of(itemName);
    }
}
