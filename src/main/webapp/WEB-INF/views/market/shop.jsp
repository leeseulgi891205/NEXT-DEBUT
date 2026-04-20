<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>SHOP - NEXT DEBUT</title>

<style>
    .modal-border-light{
        position:relative;
        border-radius:26px;
        border:1px solid rgba(255,255,255,0.42);
        background:linear-gradient(180deg,rgba(255,255,255,0.04),rgba(255,255,255,0.01));
        box-shadow:0 0 0 1px rgba(255,255,255,0.04) inset,0 0 28px rgba(180,120,255,0.14);
    }
    .modal-border-light::before{
        content:"";
        position:absolute;
        top:-50%;
        left:-80%;
        width:200%;
        height:200%;
        border-radius:26px;
        background:linear-gradient(
            110deg,
            rgba(255,255,255,0) 32%,
            rgba(255,255,255,0.10) 44%,
            rgba(255,255,255,0.18) 50%,
            rgba(255,255,255,0.10) 56%,
            rgba(255,255,255,0) 68%
        );
        transform:translateX(-120%) rotate(12deg);
        animation:mirrorSweep 3.5s linear infinite;
        pointer-events:none;
    }
    .modal-border-light::after{
        content:"";
        position:absolute;
        inset:0;
        border-radius:26px;
        box-shadow:inset 0 1px 0 rgba(255,255,255,0.08), inset 0 -1px 0 rgba(255,255,255,0.04);
        pointer-events:none;
    }
    @keyframes mirrorSweep{
        0%{transform:translateX(-120%) rotate(12deg);}
        100%{transform:translateX(120%) rotate(12deg);}
    }

    @keyframes modalOuterGlow{
        0%{
            box-shadow:
                0 0 0 1px rgba(255,255,255,0.20),
                0 0 18px rgba(244,114,182,0.14),
                0 0 34px rgba(244,114,182,0.10),
                0 26px 60px rgba(15,23,42,0.12);
        }
        100%{
            box-shadow:
                0 0 0 1px rgba(255,255,255,0.28),
                0 0 26px rgba(244,114,182,0.22),
                0 0 52px rgba(244,114,182,0.16),
                0 28px 70px rgba(15,23,42,0.16);
        }
    }

    body.modal-open{
        overflow:hidden !important;
    }

    .shop-item.shop-item--hash-focus{
        box-shadow: 0 0 0 3px rgba(244,114,182,0.55), 0 12px 40px rgba(244,114,182,0.18);
        transition: box-shadow 0.35s ease;
    }

    /* ── SHOP: 메인 팔레트 통일(밝은 배경 + 짙은 글씨) ── */
    .shop-shell{
        position: relative;
        border-radius: 30px;
        overflow: hidden;
        background: var(--card);
        border: 1px solid var(--card-border);
        box-shadow: 0 26px 90px rgba(99,102,241,0.10);
    }
    .shop-shell::before{
        content:"";
        position:absolute;
        inset:-2px;
        pointer-events:none;
        background:
            radial-gradient(circle at 18% 18%, rgba(233,176,196,0.16), transparent 58%),
            radial-gradient(circle at 78% 22%, rgba(186,198,220,0.14), transparent 62%),
            radial-gradient(circle at 50% 110%, rgba(203,186,216,0.14), transparent 55%);
        filter: blur(18px);
        opacity: 0.9;
    }
    .shop-shell > *{
        position:relative;
        z-index:1;
    }

    .shop-head .shop-kicker{ color: rgba(51,65,85,0.70) !important; }
    .shop-head .shop-title{
        color: var(--text) !important;
        background: linear-gradient(120deg, var(--text) 0%, var(--pink) 36%, var(--lv) 64%, var(--bl) 100%);
        -webkit-background-clip: text;
        background-clip: text;
        -webkit-text-fill-color: transparent;
    }
    .shop-head .shop-sub{ color: var(--text-mid) !important; }
    .shop-coin{ color: var(--text) !important; }
    .shop-coin i{ color: #f59e0b !important; }

    .coin-my-item-btn{
        position: relative;
        display:inline-flex;
        align-items:center;
        gap:8px;
        padding:10px 18px;
        border-radius:999px;
        border:1px solid rgba(255,255,255,0.6);
        font-size:15px;
        font-weight:900;
        letter-spacing:0.04em;
        cursor:pointer;

        background:
            linear-gradient(135deg,
                rgba(255,255,255,0.85),
                rgba(255,245,250,0.65),
                rgba(255,255,255,0.85)
            );

        color:#7c3aed;

        box-shadow:
            0 6px 20px rgba(244,114,182,0.15),
            inset 0 1px 0 rgba(255,255,255,0.7);

        backdrop-filter: blur(10px) saturate(120%);
        -webkit-backdrop-filter: blur(10px) saturate(120%);

        transition: all 0.18s ease;
    }

    /* 은은한 광택 흐름 */
    .coin-my-item-btn::before{
        content:"";
        position:absolute;
        inset:-2px;
        border-radius:999px;
        background:linear-gradient(
            120deg,
            transparent 30%,
            rgba(255,255,255,0.6) 45%,
            transparent 60%
        );
        opacity:0;
        transform:translateX(-120%);
        transition:all 0.6s ease;
        pointer-events:none;
    }

    .coin-my-item-btn:hover::before{
        opacity:1;
        transform:translateX(120%);
    }

    /* hover */
    .coin-my-item-btn:hover{
        transform:translateY(-2px);
        box-shadow:
            0 12px 28px rgba(244,114,182,0.25),
            inset 0 1px 0 rgba(255,255,255,0.9);
    }

    /* 클릭 */
    .coin-my-item-btn:active{
        transform:translateY(0);
        box-shadow:
            0 4px 10px rgba(244,114,182,0.2),
            inset 0 2px 4px rgba(0,0,0,0.05);
    }

    /* 아이콘 */
    .coin-my-item-btn i{
        font-size:14px;
        color:#ff6fb7;
    }

    /* 텍스트 */
    .coin-my-item-btn span{
        color:#cd5cd1;
    }

    .shop-kpi .kpi-card{
        border: 1px solid rgba(167,139,250,0.22) !important;
        background: rgba(255,255,255,0.82) !important;
    }
    .shop-kpi .kpi-k{ color: rgba(51,65,85,0.70) !important; }
    .shop-kpi .kpi-v{ color: var(--text) !important; }
    .shop-kpi .kpi-card--accent{
        border-color: rgba(244,114,182,0.28) !important;
        background: linear-gradient(135deg, rgba(244,114,182,0.12), rgba(192,132,252,0.10)) !important;
        box-shadow: 0 14px 36px rgba(244,114,182,0.12);
    }

    .shop-filters .filter-btn{
        border: 1px solid rgba(167,139,250,0.22) !important;
        background: rgba(255,255,255,0.75) !important;
        color: rgba(51,65,85,0.85) !important;
    }
    .shop-filters .filter-btn:hover{
        background: rgba(255,255,255,0.92) !important;
        border-color: rgba(244,114,182,0.35) !important;
    }
    .shop-filters .filter-btn.is-active{
        background: linear-gradient(135deg, rgba(244,114,182,0.92), rgba(192,132,252,0.88)) !important;
        color: rgba(15,23,42,0.92) !important;
        border-color: rgba(244,114,182,0.45) !important;
        box-shadow: 0 12px 28px rgba(244,114,182,0.18);
        font-weight: 900;
    }

    .shop-section-h .shop-section-label{ color: rgba(51,65,85,0.78) !important; }
    .shop-section-h .shop-section-line{ background: rgba(167,139,250,0.22) !important; }

    /* 카드 */
    .shop-shell .shop-item{
        background: rgba(255,255,255,0.82) !important;
        border-color: rgba(167,139,250,0.22) !important;
    }
    .shop-shell .shop-item:hover{
        box-shadow: 0 18px 44px rgba(99,102,241,0.14) !important;
    }
    .shop-shell .shop-item .p-3 .item-price{ color: rgba(244,114,182,0.95) !important; }
    .shop-shell .shop-item .p-3 .item-cat{ color: rgba(71,85,105,0.75) !important; }

    /* BUY 버튼 */
    .shop-shell .shop-item .buy-btn{
        background: linear-gradient(135deg, rgba(244,114,182,0.92), rgba(192,132,252,0.86), rgba(96,165,250,0.86)) !important;
        color: rgba(15,23,42,0.92) !important;
    }
    .shop-shell .shop-item .buy-btn:hover{
        filter: brightness(1.03);
    }

    /* 랜덤뽑기 */
    .shop-container-wrap{
        position: relative;
        overflow: visible;
    }

    .random-draw-sketch-btn{
        position: absolute;
        top: 72px;
        left: -165px;
        width: 165px;
        height: 44px;
        border: 0;
        padding: 0;
        background: transparent;
        cursor: pointer;
        z-index: 50;
    }

    .random-draw-sketch-btn::before{
        content: "";
        position: absolute;
        inset: 0;
        clip-path: polygon(
            0 100%,
            18px 72%,
            42px 0,
            100% 0,
            100% 100%
        );
        background:
            linear-gradient(180deg, rgba(255,255,255,0.26), rgba(255,255,255,0.10)),
            linear-gradient(135deg, rgba(255,182,193,0.22), rgba(244,114,182,0.10));
        border: 1px solid rgba(255,255,255,0.55);
        backdrop-filter: blur(8px) saturate(112%);
        -webkit-backdrop-filter: blur(8px) saturate(112%);
        box-shadow:
            inset 0 1px 0 rgba(255,255,255,0.22),
            inset 0 0 0 1px rgba(255,255,255,0.10);
    }

    .random-draw-sketch-btn::after{
        content: "";
        position: absolute;
        left: 24px;
        top: 5px;
        width: 126px;
        height: 8px;
        clip-path: polygon(
            0 100%,
            12px 25%,
            100% 0,
            100% 100%
        );
        background: linear-gradient(
            180deg,
            rgba(255,255,255,0.42),
            rgba(255,255,255,0.06)
        );
        opacity: 0.9;
        pointer-events: none;
    }

    .random-draw-sketch-btn .draw-label::before{
        content:"";
        position:absolute;
        inset:-13px -18px;
        clip-path: polygon(
            0 100%,
            18px 72%,
            42px 0,
            100% 0,
            100% 100%
        );
        border: 1px solid rgba(255,182,193,0.16);
        pointer-events:none;
        z-index:-1;
    }

    .random-draw-sketch-btn .draw-label{
        position: absolute;
        left: 40px;
        top: 50%;
        transform: translateY(-50%);
        font-size: 13px;
        font-weight: 800;
        color: rgba(78, 43, 66, 0.88);
        letter-spacing: -0.02em;
        white-space: nowrap;
        z-index: 2;
        text-shadow: none;
    }

    .random-draw-sketch-btn:hover::before{
        background:
            linear-gradient(180deg, rgba(255,255,255,0.30), rgba(255,255,255,0.12)),
            linear-gradient(135deg, rgba(255,182,193,0.26), rgba(244,114,182,0.14));
        border-color: rgba(255,255,255,0.52);
        box-shadow:
            inset 0 1px 0 rgba(255,255,255,0.26),
            inset 0 0 0 1px rgba(255,255,255,0.12);
    }

    .random-draw-sketch-btn:hover .draw-label{
        color: rgba(92, 45, 74, 0.96);
    }

    .random-draw-sketch-btn:active{
        transform: none;
    }

    /* 내 아이템 버튼 */
    .my-item-sketch-btn{
        position: absolute;
        top: 126px;
        left: -165px;
        width: 165px;
        height: 44px;
        border: 0;
        padding: 0;
        background: transparent;
        cursor: pointer;
        z-index: 50;
    }

    .my-item-sketch-btn::before{
        content: "";
        position: absolute;
        inset: 0;
        clip-path: polygon(
            0 100%,
            18px 72%,
            42px 0,
            100% 0,
            100% 100%
        );
        background:
            linear-gradient(180deg, rgba(255,255,255,0.26), rgba(255,255,255,0.10)),
            linear-gradient(135deg, rgba(196,181,253,0.22), rgba(167,139,250,0.12));
        border: 1px solid rgba(255,255,255,0.55);
        backdrop-filter: blur(8px) saturate(112%);
        -webkit-backdrop-filter: blur(8px) saturate(112%);
        box-shadow:
            inset 0 1px 0 rgba(255,255,255,0.22),
            inset 0 0 0 1px rgba(255,255,255,0.10);
    }

    .my-item-sketch-btn::after{
        content: "";
        position: absolute;
        left: 24px;
        top: 5px;
        width: 126px;
        height: 8px;
        clip-path: polygon(
            0 100%,
            12px 25%,
            100% 0,
            100% 100%
        );
        background: linear-gradient(
            180deg,
            rgba(255,255,255,0.42),
            rgba(255,255,255,0.06)
        );
        opacity: 0.9;
        pointer-events: none;
    }

    .my-item-sketch-btn .draw-label::before{
        content:"";
        position:absolute;
        inset:-13px -18px;
        clip-path: polygon(
            0 100%,
            18px 72%,
            42px 0,
            100% 0,
            100% 100%
        );
        border: 1px solid rgba(167,139,250,0.18);
        pointer-events:none;
        z-index:-1;
    }

    .my-item-sketch-btn .draw-label{
        position: absolute;
        left: 40px;
        top: 50%;
        transform: translateY(-50%);
        font-size: 13px;
        font-weight: 800;
        color: rgba(74, 52, 110, 0.92);
        letter-spacing: -0.02em;
        white-space: nowrap;
        z-index: 2;
        text-shadow: none;
    }

    .my-item-sketch-btn:hover::before{
        background:
            linear-gradient(180deg, rgba(255,255,255,0.30), rgba(255,255,255,0.12)),
            linear-gradient(135deg, rgba(196,181,253,0.26), rgba(167,139,250,0.16));
        border-color: rgba(255,255,255,0.52);
        box-shadow:
            inset 0 1px 0 rgba(255,255,255,0.26),
            inset 0 0 0 1px rgba(255,255,255,0.12);
    }

    .my-item-sketch-btn:hover .draw-label{
        color: rgba(88, 59, 138, 0.98);
    }

    .my-item-sketch-btn:active{
        transform: none;
    }

    /* 내 아이템 리스트 */
    #myItemList{
        max-height: 420px;
        overflow-y: auto;
        -ms-overflow-style:none;
        scrollbar-width:none;
    }
    #myItemList::-webkit-scrollbar{
        display:none;
    }

    .my-item-row{
        display:flex;
        align-items:center;
        gap:12px;
        padding:12px;
        border:1px solid rgba(255,255,255,0.34);
        border-radius:16px;
        background: linear-gradient(180deg, rgba(255,255,255,0.85), rgba(255,245,250,0.45));
        backdrop-filter:blur(10px);
        -webkit-backdrop-filter:blur(10px);
        transition:all 0.18s ease;
        cursor:pointer;
        min-width:0;
    }

    .my-item-thumb{
        width:64px;
        height:64px;
        border-radius:14px;
        object-fit:cover;
        flex-shrink:0;
        border:1px solid rgba(255,255,255,0.42);
        background:rgba(255,255,255,0.35);
    }

    .my-item-info{
        flex:1;
        min-width:0;
    }

    .my-item-name{
        font-size:15px;
        font-weight:800;
        color:#0f172a;
        margin-bottom:4px;
    }

    .my-item-desc{
        font-size:12px;
        color:#64748b;
        line-height:1.5;
    }

    .my-item-meta{
        font-size:12px;
        font-weight:700;
        color:#ec4899;
        white-space:nowrap;
    }

    .my-item-empty{
        text-align:center;
        padding:40px 12px;
        color:#94a3b8;
        font-size:14px;
    }

    /* 핵심: 유리 느낌 모달 */
    .item-modal{
        position: fixed;
        inset: 0;
        z-index: 99999;
        display:flex;
        align-items:center;
        justify-content:center;
        padding:24px;
        background: rgba(255,240,245,0.35);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
    }

    .item-modal.hidden{
        display: none !important;
    }

    .item-modal-dialog{
    position: relative;
    left: auto;
    top: auto;
    transform: none;
    width: min(92vw, 430px);
    max-width: 430px;
    max-height: 90vh;
    margin: 0;
    overflow-y: auto;
    overflow-x: hidden;
    isolation: isolate;

    -ms-overflow-style: none;
    scrollbar-width: none;

    border-radius: 28px;
    border: 1px solid rgba(255,255,255,0.72);

    background:
        linear-gradient(
            135deg,
            rgba(255,255,255,0.88),
            rgba(255,245,250,0.68),
            rgba(255,255,255,0.82)
        );

    box-shadow:
        0 0 0 1px rgba(255,255,255,0.20),
        0 0 22px rgba(244,114,182,0.16),
        0 0 44px rgba(244,114,182,0.10),
        0 26px 60px rgba(15,23,42,0.14),
        inset 0 1px 0 rgba(255,255,255,0.72),
        inset 0 -1px 0 rgba(255,255,255,0.20);

    backdrop-filter: blur(10px) saturate(122%);
    -webkit-backdrop-filter: blur(10px) saturate(122%);
    animation: modalOuterGlow 2.8s ease-in-out infinite alternate;
}

.item-modal-dialog::-webkit-scrollbar{
    display:none;
}

    /* 내부 유리광택 */
    .item-modal-dialog::before{
        content:"";
        position:absolute;
        inset:0;
        border-radius:28px;
        pointer-events:none;
        z-index:0;
        background:
            linear-gradient(
                135deg,
                rgba(255,255,255,0.42) 0%,
                rgba(255,245,250,0.18) 35%,
                rgba(255,255,255,0.08) 55%,
                rgba(255,255,255,0.22) 100%
            );
    }

    /* 외곽 핑크빛 aura */
    .item-modal-dialog::after{
        content:"";
        position:absolute;
        inset:-8px;
        border-radius:34px;
        pointer-events:none;
        z-index:-1;
        background:
            radial-gradient(circle at 50% 50%,
                rgba(244,114,182,0.22) 0%,
                rgba(244,114,182,0.10) 45%,
                rgba(244,114,182,0.00) 74%);
        filter: blur(12px);
        opacity: 1;
    }

    /* 모달 안 내용이 광택 위에 보이도록 */
    .item-modal-dialog > *{
        position: relative;
        z-index: 1;
    }

    /* 내아이템목록 - 정보 */
    #myItemModalDialog{
        width: min(96vw, 980px) !important;
        max-width: 980px !important;
    }

    .my-item-modal-layout{
        display:grid;
        grid-template-columns: 340px minmax(0, 1fr);
        gap:18px;
        padding:20px;
        min-height:520px;
        align-items:stretch;
    }

    .my-item-preview{
        border:1px solid rgba(255,255,255,0.34);
        border-radius:20px;
        background: linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,245,250,0.40));
        padding:16px;
        display:flex;
        flex-direction:column;
        min-width:0;
        min-height:440px;
        box-shadow:
            inset 0 1px 0 rgba(255,255,255,0.36),
            0 10px 30px rgba(99,102,241,0.10);
        backdrop-filter:blur(14px);
        -webkit-backdrop-filter:blur(14px);
    }

    .my-item-preview-thumb{
        width:100%;
        height:220px;
        border-radius:16px;
        object-fit:cover;
        border:1px solid rgba(255,255,255,0.42);
        background:rgba(255,255,255,0.30);
        margin-bottom:14px;
    }

    .my-item-preview-name{
        font-size:24px;
        font-weight:900;
        color:#0f172a;
        margin-bottom:10px;
        line-height:1.35;
        word-break:keep-all;
    }

    .my-item-preview-effect{
        font-size:15px;
        font-weight:800;
        color:#ec4899;
        margin-bottom:12px;
        line-height:1.6;
        word-break:keep-all;
    }

    .my-item-preview-desc{
        font-size:14px;
        color:#64748b;
        line-height:1.75;
        word-break:keep-all;
    }

    .my-item-preview-qty{
        margin-top:auto;
        padding-top:16px;
        font-size:14px;
        font-weight:900;
        color:#7c3aed;
    }

    .my-item-preview-empty{
        display:flex;
        align-items:center;
        justify-content:center;
        text-align:center;
        width:100%;
        height:100%;
        min-height:408px;
        color:#94a3b8;
        font-size:14px;
        line-height:1.8;
        padding:12px;
    }

    .my-item-list-wrap{
        min-width:0;
        border:1px solid rgba(255,255,255,0.34);
        border-radius:20px;
        background: linear-gradient(180deg, rgba(255,255,255,0.70), rgba(255,245,250,0.35));
        padding:12px;
        box-shadow:
            inset 0 1px 0 rgba(255,255,255,0.34),
            0 10px 30px rgba(99,102,241,0.08);
        backdrop-filter:blur(14px);
        -webkit-backdrop-filter:blur(14px);
    }

    #myItemList{
        max-height:440px;
        overflow-y:auto;
        overflow-x:hidden;
        padding-right:4px;
        -ms-overflow-style:none;
        scrollbar-width:none;
    }
    #myItemList::-webkit-scrollbar{
        display:none;
    }

    .my-item-row + .my-item-row{
        margin-top:10px;
    }

    .my-item-row:hover{
        border-color:rgba(255,255,255,0.50);
        box-shadow:
            0 10px 24px rgba(99,102,241,0.12),
            inset 0 1px 0 rgba(255,255,255,0.26);
        transform:translateX(2px);
    }

    .my-item-thumb{
        width:72px;
        height:72px;
        border-radius:14px;
        object-fit:cover;
        flex-shrink:0;
        border:1px solid rgba(255,255,255,0.42);
    }

    .my-item-info{
        flex:1;
        min-width:0;
    }

    .my-item-name{
        font-size:15px;
        font-weight:800;
        color:#0f172a;
        margin-bottom:4px;
        line-height:1.4;
        word-break:keep-all;
    }

    .my-item-desc{
        font-size:12px;
        color:#64748b;
        line-height:1.5;
        word-break:keep-all;
    }

    .my-item-meta{
        font-size:13px;
        font-weight:800;
        color:#ec4899;
        white-space:nowrap;
        flex-shrink:0;
        margin-left:8px;
    }

    /* 닫기 버튼 유리톤 */
    .item-modal-dialog button[type="button"]{
        background: rgba(255,255,255,0.34) !important;
        border: 1px solid rgba(255,255,255,0.52) !important;
        box-shadow:
            inset 0 1px 0 rgba(255,255,255,0.34),
            0 6px 18px rgba(15,23,42,0.08);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
    }

    .item-modal-dialog button[type="button"]:hover{
        background: rgba(255,255,255,0.48) !important;
    }

    /* 모달 BUY 버튼 - 게임 스타일 (단색) */
    #itemModal .buy-btn{
        background: rgba(255,182,193,0.65);
        color: #9d174d !important;
        border: 1.5px solid rgba(244,114,182,0.45);
        border-radius: 999px;
        box-shadow:
            0 6px 14px rgba(244,114,182,0.18),
            inset 0 1px 0 rgba(255,255,255,0.6);
        transition: all 0.15s ease;
    }

    #itemModal .buy-btn:hover{
        background: rgba(255,182,193,0.85);
        box-shadow:
            0 10px 20px rgba(244,114,182,0.25);
        transform: translateY(-1px);
    }

    #itemModal .buy-btn:active{
        transform: translateY(1px);
        box-shadow:
            0 3px 6px rgba(244,114,182,0.2),
            inset 0 2px 4px rgba(0,0,0,0.08);
    }

    @media (max-width: 900px){
        #myItemModalDialog{
            width: min(96vw, 700px) !important;
            max-width: 700px !important;
        }

        .my-item-modal-layout{
            grid-template-columns: 1fr;
        }

        .my-item-preview{
            min-height:auto;
        }

        .my-item-preview-empty{
            min-height:180px;
        }

        #myItemList{
            max-height:300px;
        }
    }

    /* 모달 등장 애니메이션 */
    @keyframes modalFadeUp {
        0%{
            opacity: 0;
            transform: translateY(30px) scale(0.96);
        }
        100%{
            opacity: 1;
            transform: translateY(0) scale(1);
        }
    }

    /* 배경도 부드럽게 */
    @keyframes modalBackdropFade {
        0%{ opacity: 0; }
        100%{ opacity: 1; }
    }

    /* 모달 배경 */
    .item-modal{
        animation: modalBackdropFade 0.25s ease;
    }

    /* 실제 모달 */
    .item-modal-dialog{
        animation: modalFadeUp 0.35s cubic-bezier(0.22, 1, 0.36, 1);
    }


    @keyframes modalFadeUp {
        0%{
            opacity: 0;
            transform: translateY(30px) scale(0.96);
        }
        100%{
            opacity: 1;
            transform: translateY(0) scale(1);
        }
    }

    @keyframes modalBackdropFade {
        0%{ opacity: 0; }
        100%{ opacity: 1; }
    }

    .item-modal{
        animation: modalBackdropFade 0.22s ease;
    }

    .item-modal-dialog{
        animation: modalFadeUp 0.32s cubic-bezier(0.22, 1, 0.36, 1);
    }

    .coin-charge-btn{
        color:#b45309; /* 글씨 주황 */

        background:
            linear-gradient(135deg,
                rgba(255,255,255,0.9),
                rgba(255,237,213,0.85),
                rgba(255,255,255,0.9)
            );

        border:1px solid rgba(251,146,60,0.4);

        box-shadow:
            0 6px 20px rgba(251,146,60,0.25),
            inset 0 1px 0 rgba(255,255,255,0.7);
    }

    /* hover */
    .coin-charge-btn:hover{
        box-shadow:
            0 12px 30px rgba(251,146,60,0.35),
            inset 0 1px 0 rgba(255,255,255,0.9);
    }

    /* 아이콘 */
    .coin-charge-btn i{
        color:#f97316;
    }

    .coin-charge-btn span{
        color:#ea580c;
    }

    .coin-item-btn{
        color:#be185d;
        background:
            linear-gradient(135deg,
                rgba(255,255,255,0.9),
                rgba(255,240,245,0.85),
                rgba(255,255,255,0.9)
            );
        border:1px solid rgba(244,114,182,0.45);
        box-shadow:
            0 6px 20px rgba(244,114,182,0.25),
            inset 0 1px 0 rgba(255,255,255,0.7);
    }

    .coin-item-btn:hover{
        box-shadow:
            0 12px 30px rgba(244,114,182,0.35),
            inset 0 1px 0 rgba(255,255,255,0.9);
    }

    .coin-item-btn i{
        color:#ec4899;
    }

    .coin-item-btn span{
        color:#cd5cd1;
    }

    /* 카카오페이 모달 */
    .kakao-modal{
    position:fixed;
    top:0;
    right:-420px;
    width:400px;
    max-width:90%;
    height:100vh;
    background:rgba(0,0,0,0.35);
    backdrop-filter:blur(6px);
    transition:right 0.35s ease;
    z-index:99999;
}

.kakao-modal.show{
    right:0;
}

.kakao-modal-content{
    position:absolute;
    top:0;
    right:0;
    width:100%;
    height:100%;
    background:#fff;
    box-shadow:-8px 0 24px rgba(0,0,0,0.18);
    padding:24px 20px;
    box-sizing:border-box;
}

.kakao-modal-header{
    display:flex;
    justify-content:space-between;
    align-items:center;
    font-size:20px;
    font-weight:700;
    margin-bottom:20px;
}

#kakaoCloseBtn{
    border:none;
    background:none;
    font-size:28px;
    cursor:pointer;
}

.kakao-modal-body p{
    margin:0 0 16px;
    font-size:15px;
    color:#444;
}

.charge-list{
    display:flex;
    flex-direction:column;
    gap:10px;
    margin-bottom:20px;
}

.charge-item{
    width:100%;
    padding:14px;
    border:1px solid #ddd;
    border-radius:12px;
    background:#f8f8f8;
    cursor:pointer;
    font-weight:700;
}

.charge-item.active{
    background:#ffe812;
    border-color:#d6bf00;
}

.kakao-pay-btn{
    width:100%;
    padding:15px;
    border:none;
    border-radius:12px;
    background:#ffe812;
    font-weight:800;
    font-size:16px;
    cursor:pointer;
}

/* KakaoPay */
.charge-item.active{
    border:2px solid #f7d000;
    background:#fff9cc;
}

/* 상점/뽑기 탭 */
.board-filter-bar { display:flex; flex-wrap:wrap; align-items:center; gap:8px; }
.board-filter-bar .filter-chip {
    display:inline-flex; align-items:center; gap:6px; padding:8px 18px; border-radius:999px; font-size:13px; font-weight:600;
    border:1px solid rgba(148,163,184,0.35); background:rgba(255,255,255,0.55); color:rgba(71,85,105,0.88);
    text-decoration:none; transition:all 200ms ease;
}
.board-filter-bar .filter-chip:hover { background:rgba(255,255,255,0.92); color:#0f172a; }
.board-filter-bar .filter-chip.is-active {
    background:linear-gradient(135deg,rgba(233,176,196,0.88),rgba(204,186,216,0.85));
    color:rgba(20,10,30,0.95); border-color:transparent;
}

</style>

</head>

<body class="page-main min-h-screen flex flex-col">

<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">
    <div class="container mx-auto max-w-5xl shop-container-wrap">

        <section class="shop-shell px-8 py-10 md:px-10 md:py-12">

            <div class="board-filter-bar mb-6">
                <a href="${pageContext.request.contextPath}/market/shop" class="filter-chip ${empty marketTab or marketTab eq 'shop' ? 'is-active' : ''}">아이템</a>
                <a href="${pageContext.request.contextPath}/market/gacha" class="filter-chip ${marketTab eq 'gacha' ? 'is-active' : ''}">연습생 뽑기</a>
                <a href="${pageContext.request.contextPath}/market/photocard" class="filter-chip ${marketTab eq 'photocard' ? 'is-active' : ''}">포토카드 뽑기</a>
            </div>

            <!-- 상단 타이틀 -->
            <div class="shop-head mb-8 flex items-start justify-between gap-4 relative">
                <div>
                    <div class="shop-kicker text-[11px] tracking-[0.35em] uppercase font-orbitron mb-3">
                        NEXT DEBUT
                    </div>
                    <h1 class="shop-title font-orbitron text-4xl md:text-6xl font-black leading-none mb-4">
                        아이템 상점
                    </h1>
                    <p class="shop-sub text-sm md:text-base">
                        적용 후 매 게임 일마다 해당 스탯이 +1씩 누적되는 성장 아이템(1주·2주)을 구매할 수 있습니다.
                    </p>
                </div>

                <div class="absolute top-6 right-8 flex flex-col items-end gap-3 z-20">
                    <div class="shop-coin text-lg font-bold flex items-center gap-2">
                        <i class="fas fa-coins text-yellow-300"></i>
                        COIN : <span id="currentCoinText">${empty currentCoin ? 1000 : currentCoin}</span>
                    </div>
                
                    <button type="button" class="coin-my-item-btn coin-item-btn" onclick="openMyItemPage()">
                        <i class="fas fa-box-open"></i>
                        <span>내 아이템</span>
                    </button>
                    <!-- 카카오페이 -->
                    <button type="button" class="coin-my-item-btn coin-charge-btn" id="coinChargeBtn">
                        <i class="fas fa-coins"></i>
                        <span>충전하기</span>
                    </button>
                </div>
            </div>


            <!-- 필터 버튼 -->
            <div class="shop-filters flex flex-wrap gap-3 mb-8" id="shopFilterButtons">
                <button type="button" data-filter="all" class="filter-btn is-active px-5 py-2 rounded-full text-sm transition">전체</button>
                <button type="button" data-filter="vocal" class="filter-btn px-5 py-2 rounded-full text-sm transition">보컬</button>
                <button type="button" data-filter="dance" class="filter-btn px-5 py-2 rounded-full text-sm transition">댄스</button>
                <button type="button" data-filter="star" class="filter-btn px-5 py-2 rounded-full text-sm transition">스타성</button>
                <button type="button" data-filter="mental" class="filter-btn px-5 py-2 rounded-full text-sm transition">멘탈</button>
                <button type="button" data-filter="teamwork" class="filter-btn px-5 py-2 rounded-full text-sm transition">팀워크</button>
            </div>

            <!-- 섹션 제목 -->
            <div class="shop-section-h flex items-center gap-4 mb-5">
                <div class="shop-section-label text-[13px] font-orbitron tracking-[0.28em] uppercase">
                    SHOP ITEMS
                </div>
                <div class="shop-section-line flex-1 h-px"></div>
            </div>

            <!-- 상품 카드 -->
            <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">

                <!-- 11 전체 +1 -->
                <div id="shop-item-allpass" class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-amber-200/60" data-category="special">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '올라운드 패키지 박스',
                            '+10 아이템 5종 패키지',
                            '보컬 워터, 댄스 슈즈, 팬레터, 릴렉스 캔디, 팀 스낵 박스를 각 1개씩 묶어서 구매하는 패키지입니다.',
                            '900 COIN',
                            '${pageContext.request.contextPath}/images/items/allpass.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/allpass.png" alt="올라운드 패키지 박스" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 left-2 px-2 py-0.5 rounded-lg bg-yellow-400 text-[#2b1600] text-[10px] font-bold">SPECIAL</div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-amber-300/25 border border-amber-200/40 flex items-center justify-center text-amber-100 text-xs">
                            <i class="fas fa-star"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">올라운드 패키지 박스</div>
                            <div class="text-slate-600 text-[10px] mb-1">보컬,댄스,스타성,멘탈,팀워크 +10</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#021</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">900 COIN</span>
                            <span class="item-cat text-[10px]">스페셜</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="올라운드 패키지 박스" data-item-effect="+10 아이템 5종 패키지" data-item-desc="보컬 워터, 댄스 슈즈, 팬레터, 릴렉스 캔디, 팀 스낵 박스를 각 1개씩 묶어서 구매하는 패키지입니다." data-item-price="900 COIN" data-item-image="${pageContext.request.contextPath}/images/items/allpass.png">아이템 구매</button>
                    </div>
                </div>

                <!-- 1 보컬 +1 -->
                <div id="shop-item-water" class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="vocal">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '보컬 워터',
                            '보컬 +10',
                            '기본 성대 컨디션 관리용 워터입니다. 보컬 능력치가 +10 적용됩니다.',
                            '180 COIN',
                            '${pageContext.request.contextPath}/images/items/water.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/water.png" alt="보컬 워터" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-[#1e2b45]/80 border border-sky-300/60 flex items-center justify-center text-sky-300 text-xs shadow">
                            <i class="fas fa-microphone"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">보컬 워터</div>
                            <div class="text-slate-600 text-[10px] mb-1">보컬 +10</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#011</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">180 COIN</span>
                            <span class="item-cat text-[10px]">보컬</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="보컬 워터" data-item-effect="보컬 +10" data-item-desc="기본 성대 컨디션 관리용 워터입니다. 보컬 능력치가 +10 적용됩니다." data-item-price="180 COIN" data-item-image="${pageContext.request.contextPath}/images/items/water.png">아이템 구매</button>
                    </div>
                </div>

                <!-- 2 보컬 +2 -->
                <div id="shop-item-breathe" class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="vocal">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '호흡 컨트롤 북',
                            '보컬 +20',
                            '호흡 조절 훈련을 위한 성장형 교재입니다. 보컬 능력치가 +20 적용됩니다.',
                            '320 COIN',
                            '${pageContext.request.contextPath}/images/items/breathe control.jpg'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/breathe control.jpg" alt="호흡 컨트롤 북" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-[#1e2b45]/80 border border-sky-300/60 flex items-center justify-center text-sky-300 text-xs shadow">
                            <i class="fas fa-book-open"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">호흡 컨트롤 북</div>
                            <div class="text-slate-600 text-[10px] mb-1">보컬 +20</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#012</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">320 COIN</span>
                            <span class="item-cat text-[10px]">보컬</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="호흡 컨트롤 북" data-item-effect="보컬 +20" data-item-desc="호흡 조절 훈련을 위한 성장형 교재입니다. 보컬 능력치가 +20 적용됩니다." data-item-price="320 COIN" data-item-image="${pageContext.request.contextPath}/images/items/breathe control.jpg">아이템 구매</button>
                    </div>
                </div>

                <!-- 3 댄스 +1 -->
                <div id="shop-item-shoes" class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="dance">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '댄스 슈즈',
                            '댄스 +10',
                            '기초 안무 훈련에 적합한 연습용 슈즈입니다.',
                            '180 COIN',
                            '${pageContext.request.contextPath}/images/items/shoes.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/shoes.png" alt="댄스 슈즈" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-violet-300/25 border border-violet-200/40 flex items-center justify-center text-violet-100 text-xs">
                            <i class="fas fa-shoe-prints"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">댄스 슈즈</div>
                            <div class="text-slate-600 text-[10px] mb-1">댄스 +10</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#013</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">180 COIN</span>
                            <span class="item-cat text-[10px]">댄스</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="댄스 슈즈" data-item-effect="댄스 +10" data-item-desc="기초 안무 훈련에 적합한 연습용 슈즈입니다." data-item-price="180 COIN" data-item-image="${pageContext.request.contextPath}/images/items/shoes.png">아이템 구매</button>
                    </div>
                </div>

                <!-- 4 댄스 +2 -->
                <div id="shop-item-band" class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="dance">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '퍼포먼스 밴드',
                            '댄스 +20',
                            '퍼포먼스 안정성과 동작 완성도를 높여주는 훈련용 밴드입니다.',
                            '320 COIN',
                            '${pageContext.request.contextPath}/images/items/band.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/band.png" alt="퍼포먼스 밴드" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-violet-300/25 border border-violet-200/40 flex items-center justify-center text-violet-100 text-xs">
                            <i class="fas fa-running"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">퍼포먼스 밴드</div>
                            <div class="text-slate-600 text-[10px] mb-1">댄스 +20</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#014</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">320 COIN</span>
                            <span class="item-cat text-[10px]">댄스</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="퍼포먼스 밴드" data-item-effect="댄스 +20" data-item-desc="퍼포먼스 안정성과 동작 완성도를 높여주는 훈련용 밴드입니다." data-item-price="320 COIN" data-item-image="${pageContext.request.contextPath}/images/items/band.png">아이템 구매</button>
                    </div>
                </div>

                <!-- 5 스타성 +1 -->
                <div id="shop-item-letter" class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="star">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '팬레터',
                            '스타성 +10',
                            '팬 반응 훈련과 이미지 감각을 키워주는 기본 홍보 아이템입니다.',
                            '180 COIN',
                            '${pageContext.request.contextPath}/images/items/letter.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/letter.png" alt="팬레터" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-amber-300/25 border border-amber-200/40 flex items-center justify-center text-amber-100 text-xs">
                            <i class="fas fa-envelope-open-text"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">팬레터</div>
                            <div class="text-slate-600 text-[10px] mb-1">스타성 +10</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#015</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">180 COIN</span>
                            <span class="item-cat text-[10px]">스타성</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="팬레터" data-item-effect="스타성 +10" data-item-desc="팬 반응 훈련과 이미지 감각을 키워주는 기본 홍보 아이템입니다." data-item-price="180 COIN" data-item-image="${pageContext.request.contextPath}/images/items/letter.png">아이템 구매</button>
                    </div>
                </div>

                <!-- 6 스타성 +2 -->
                <div id="shop-item-live" class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="star">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '라이브 방송 세트',
                            '스타성 +20',
                            '방송 감각과 주목도를 끌어올리는 홍보 세트입니다.',
                            '320 COIN',
                            '${pageContext.request.contextPath}/images/items/live.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/live.png" alt="라이브 방송 세트" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-amber-300/25 border border-amber-200/40 flex items-center justify-center text-amber-100 text-xs">
                            <i class="fas fa-video"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">라이브 방송 세트</div>
                            <div class="text-slate-600 text-[10px] mb-1">스타성 +20</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#016</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">320 COIN</span>
                            <span class="item-cat text-[10px]">스타성</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="라이브 방송 세트" data-item-effect="스타성 +20" data-item-desc="방송 감각과 주목도를 끌어올리는 홍보 세트입니다." data-item-price="320 COIN" data-item-image="${pageContext.request.contextPath}/images/items/live.png">아이템 구매</button>
                    </div>
                </div>

                <!-- 7 멘탈 +1 -->
                <div id="shop-item-candy" class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="mental">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '릴렉스 캔디',
                            '멘탈 +10',
                            '짧은 기간 동안 긴장을 완화해 주는 회복형 아이템입니다.',
                            '180 COIN',
                            '${pageContext.request.contextPath}/images/items/candy.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/candy.png" alt="릴렉스 캔디" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-rose-300/25 border border-rose-200/40 flex items-center justify-center text-rose-100 text-xs">
                            <i class="fas fa-candy-cane"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">릴렉스 캔디</div>
                            <div class="text-slate-600 text-[10px] mb-1">멘탈 +10</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#017</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">180 COIN</span>
                            <span class="item-cat text-[10px]">멘탈</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="릴렉스 캔디" data-item-effect="멘탈 +10" data-item-desc="짧은 기간 동안 긴장을 완화해 주는 회복형 아이템입니다." data-item-price="180 COIN" data-item-image="${pageContext.request.contextPath}/images/items/candy.png">아이템 구매</button>
                    </div>
                </div>

                <!-- 8 멘탈 +2 -->
                <div id="shop-item-meditation" class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="mental">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '명상 키트',
                            '멘탈 +20',
                            '집중력과 안정감을 길러주는 관리형 키트입니다.',
                            '320 COIN',
                            '${pageContext.request.contextPath}/images/items/meditation.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/meditation.png" alt="명상 키트" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-rose-300/25 border border-rose-200/40 flex items-center justify-center text-rose-100 text-xs">
                            <i class="fas fa-leaf"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">명상 키트</div>
                            <div class="text-slate-600 text-[10px] mb-1">멘탈 +20</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#018</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">320 COIN</span>
                            <span class="item-cat text-[10px]">멘탈</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="명상 키트" data-item-effect="멘탈 +20" data-item-desc="집중력과 안정감을 길러주는 관리형 키트입니다." data-item-price="320 COIN" data-item-image="${pageContext.request.contextPath}/images/items/meditation.png">아이템 구매</button>
                    </div>
                </div>

                <!-- 9 팀워크 +1 -->
                <div class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="teamwork">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '팀 스낵 박스',
                            '팀워크 +10',
                            '연습실 분위기를 부드럽게 만들어 주는 간식 박스입니다.',
                            '180 COIN',
                            '${pageContext.request.contextPath}/images/items/snack-box.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/snack-box.png" alt="팀 스낵 박스" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-emerald-300/25 border border-emerald-200/40 flex items-center justify-center text-emerald-100 text-xs">
                            <i class="fas fa-users"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">팀 스낵 박스</div>
                            <div class="text-slate-600 text-[10px] mb-1">팀워크 +10</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#019</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">180 COIN</span>
                            <span class="item-cat text-[10px]">팀워크</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="팀 스낵 박스" data-item-effect="팀워크 +10" data-item-desc="연습실 분위기를 부드럽게 만들어 주는 간식 박스입니다." data-item-price="180 COIN" data-item-image="${pageContext.request.contextPath}/images/items/snack-box.png">아이템 구매</button>
                    </div>
                </div>

                <!-- 10 팀워크 +2 -->
                <div class="shop-item group rounded-[18px] overflow-hidden transition-all duration-200 ease-out hover:scale-[1.015] hover:border-pink-300/40" data-category="teamwork">
                    <div class="relative aspect-[3/4] overflow-hidden cursor-pointer"
                         onclick="openItemModal(
                            '유닛 워크북',
                            '팀워크 +20',
                            '합동 훈련과 역할 분담 능력을 높여주는 팀 전용 워크북입니다.',
                            '320 COIN',
                            '${pageContext.request.contextPath}/images/items/workbook.png'
                         )">
                        <img src="${pageContext.request.contextPath}/images/items/workbook.png" alt="유닛 워크북" class="w-full h-full object-cover group-hover:scale-105 transition duration-300">
                        <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/30 to-transparent"></div>
                        <div class="absolute top-2 right-2 w-7 h-7 rounded-full bg-emerald-300/25 border border-emerald-200/40 flex items-center justify-center text-emerald-100 text-xs">
                            <i class="fas fa-handshake"></i>
                        </div>
                        <div class="absolute bottom-3 left-3 right-3">
                            <div class="text-slate-900 font-bold text-sm mb-1">유닛 워크북</div>
                            <div class="text-slate-600 text-[10px] mb-1">팀워크 +20</div>
                            <div class="text-pink-200 font-orbitron text-[10px]">#020</div>
                        </div>
                    </div>
                    <div class="p-3">
                        <div class="flex items-center justify-between mb-2">
                            <span class="item-price font-bold text-xs">320 COIN</span>
                            <span class="item-cat text-[10px]">팀워크</span>
                        </div>
                        <button class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition" data-item-name="유닛 워크북" data-item-effect="팀워크 +20" data-item-desc="합동 훈련과 역할 분담 능력을 높여주는 팀 전용 워크북입니다." data-item-price="320 COIN" data-item-image="${pageContext.request.contextPath}/images/items/workbook.png">아이템 구매</button>
                    </div>
                </div>

            </div>
        </section>
    </div>
</main>

<!-- 내 아이템 모달 -->
<div id="myItemModal" class="item-modal hidden" onclick="handleMyItemModalBackdrop(event)">
    <div id="myItemModalDialog" class="item-modal-dialog" onclick="event.stopPropagation()">

        <button type="button"
                onclick="closeMyItemModal()"
                class="absolute top-4 right-4 z-10 w-9 h-9 rounded-full bg-white/80 hover:bg-white text-slate-700 border border-slate-200 flex items-center justify-center">
            <i class="fas fa-times"></i>
        </button>

        <div class="px-5 pt-5 pb-3 border-b border-slate-200/70">
            <div class="text-slate-500 text-[11px] font-orbitron tracking-[0.2em] mb-2">MY ITEMS</div>
            <div class="text-slate-900 text-2xl font-black">내 아이템</div>
            <p class="text-slate-600 text-sm mt-2">구매한 아이템 목록입니다.</p>
        </div>
        
        <div class="my-item-modal-layout">
            <!-- 왼쪽 상세 정보 -->
            <div id="myItemPreview" class="my-item-preview">
                <div class="my-item-preview-empty">
                    오른쪽 목록에 마우스를 올리면<br>아이템 상세 정보가 표시됩니다.
                </div>
            </div>
        
            <!-- 오른쪽 목록 -->
            <div class="my-item-list-wrap">
                <div id="myItemList">
                    <div class="text-slate-400 text-sm">불러오는 중...</div>
                </div>
            </div>
        </div>

    </div>
</div>


<!-- 모달 -->
<div id="itemModal" class="item-modal hidden" onclick="handleItemModalBackdrop(event)">
    <div id="itemModalDialog" class="item-modal-dialog" onclick="event.stopPropagation()">

        <button type="button"
                onclick="closeItemModal()"
                class="absolute top-4 right-4 z-10 w-9 h-9 rounded-full bg-white/80 hover:bg-white text-slate-700 border border-slate-200 flex items-center justify-center">
            <i class="fas fa-times"></i>
        </button>

        <div class="relative h-56 overflow-hidden">
            <img id="modalItemImage" class="w-full h-full object-cover" alt="">
            <div class="absolute inset-0 bg-gradient-to-t from-white/90 via-white/20 to-transparent"></div>

            <div class="absolute bottom-4 left-4 right-4">
                <div id="modalItemName" class="text-slate-900 text-2xl font-black mb-1"></div>
                <div id="modalItemEffect" class="text-pink-600 text-sm font-semibold"></div>
            </div>
        </div>

        <div class="p-5">
            <div class="mb-4">
                <div class="text-slate-500 text-[11px] font-orbitron tracking-[0.2em] mb-2">ITEM DESCRIPTION</div>
                <p id="modalItemDesc" class="text-slate-700 text-sm leading-7"></p>
            </div>

            <div class="flex justify-between mb-4">
                <span class="text-slate-500 text-sm">가격</span>
                <span id="modalItemPrice" class="text-pink-400 font-bold text-lg"></span>
            </div>

            <button id="modalBuyBtn"
                    onclick="buyCurrentItem()"
                    class="buy-btn w-full rounded-full py-2 text-[11px] font-extrabold tracking-[0.08em] transition">
                아이템 구매
            </button>
        </div>
    </div>
</div>

<script>
var contextPath = '${pageContext.request.contextPath}';

// 내아이템 설명
function getItemMeta(itemName) {
    const metaMap = {
        "올라운드 패키지 박스": {
            image: contextPath + "/images/items/allpass.png",
            effect: "보컬 워터 · 댄스 슈즈 · 팬레터 · 릴렉스 캔디 · 팀 스낵 박스 각 1개",
            desc: "+10 아이템 5종을 900 COIN에 묶어서 구매하는 패키지입니다."
        },
        "보컬 워터": {
            image: contextPath + "/images/items/water.png",
            effect: "보컬 +10",
            desc: "기본 성대 컨디션 관리용 워터입니다. 보컬 능력치가 +10 적용됩니다."
        },
        "호흡 컨트롤 북": {
            image: contextPath + "/images/items/breathe control.jpg",
            effect: "보컬 +20",
            desc: "호흡 조절 훈련을 위한 성장형 교재입니다. 보컬 능력치가 +20 적용됩니다."
        },
        "댄스 슈즈": {
            image: contextPath + "/images/items/shoes.png",
            effect: "댄스 +10",
            desc: "기초 안무 훈련에 적합한 연습용 슈즈입니다."
        },
        "퍼포먼스 밴드": {
            image: contextPath + "/images/items/band.png",
            effect: "댄스 +20",
            desc: "퍼포먼스 안정성과 동작 완성도를 높여주는 훈련용 밴드입니다."
        },
        "팬레터": {
            image: contextPath + "/images/items/letter.png",
            effect: "스타성 +10",
            desc: "팬 반응 훈련과 이미지 감각을 키워주는 기본 홍보 아이템입니다."
        },
        "라이브 방송 세트": {
            image: contextPath + "/images/items/live.png",
            effect: "스타성 +20",
            desc: "방송 감각과 주목도를 끌어올리는 홍보 세트입니다."
        },
        "릴렉스 캔디": {
            image: contextPath + "/images/items/candy.png",
            effect: "멘탈 +10",
            desc: "짧은 기간 동안 긴장을 완화해 주는 회복형 아이템입니다."
        },
        "명상 키트": {
            image: contextPath + "/images/items/meditation.png",
            effect: "멘탈 +20",
            desc: "집중력과 안정감을 길러주는 관리형 키트입니다."
        },
        "팀 스낵 박스": {
            image: contextPath + "/images/items/snack-box.png",
            effect: "팀워크 +10",
            desc: "연습실 분위기를 부드럽게 만들어 주는 간식 박스입니다."
        },
        "유닛 워크북": {
            image: contextPath + "/images/items/workbook.png",
            effect: "팀워크 +20",
            desc: "합동 훈련과 역할 분담 능력을 높여주는 팀 전용 워크북입니다."
        }
    };

    return metaMap[itemName] || {
        image: contextPath + "/images/items/default.png",
        effect: "효과 정보 없음",
        desc: "아이템 설명이 없습니다."
    };
}

function resetMyItemPreview() {
    const preview = document.getElementById("myItemPreview");
    if (!preview) return;

    preview.innerHTML = '<div class="my-item-preview-empty">' +
        '아이템 목록에 마우스를 올리면<br>상세 정보가 표시됩니다.' +
        '</div>';
}

function showMyItemPreview(item) {
    const preview = document.getElementById("myItemPreview");
    if (!preview) return;

    const itemName = item.itemName || "이름 없음";
    const quantity = item.quantity || 1;

    const meta = getItemMeta(itemName);
    const imagePath = item.imagePath && item.imagePath.trim() !== ""
        ? item.imagePath
        : meta.image;

    const effect = item.itemEffect && item.itemEffect.trim() !== ""
        ? item.itemEffect
        : meta.effect;

    const desc = meta.desc;

    var safeName = escapeHtml(itemName);
    var safeImg = escapeHtml(imagePath);
    var safeEffect = escapeHtml(effect);
    var safeDesc = escapeHtml(desc);
    var safeQty = escapeHtml(String(quantity));

    preview.innerHTML =
        '<img class="my-item-preview-thumb" src="' + safeImg + '" alt="' + safeName + '"' +
        ' onerror="this.onerror=null;this.src=\'' + contextPath + '/images/items/default.png\'">' +
        '<div class="my-item-preview-name">' + safeName + '</div>' +
        '<div class="my-item-preview-effect">' + safeEffect + '</div>' +
        '<div class="my-item-preview-desc">' + safeDesc + '</div>' +
        '<div class="my-item-preview-qty">보유 수량 : ' + safeQty + '</div>';
}

function getItemImagePath(itemName) {
    const imageMap = {
        "올라운드 패키지 박스": contextPath + "/images/items/allpass.png",
        "보컬 워터": contextPath + "/images/items/water.png",
        "호흡 컨트롤 북": contextPath + "/images/items/breathe control.jpg",
        "댄스 슈즈": contextPath + "/images/items/shoes.png",
        "퍼포먼스 밴드": contextPath + "/images/items/band.png",
        "팬레터": contextPath + "/images/items/letter.png",
        "라이브 방송 세트": contextPath + "/images/items/live.png",
        "릴렉스 캔디": contextPath + "/images/items/candy.png",
        "명상 키트": contextPath + "/images/items/meditation.png",
        "팀 스낵 박스": contextPath + "/images/items/snack-box.png",
        "유닛 워크북": contextPath + "/images/items/workbook.png"
    };

    return imageMap[itemName] || contextPath + "/images/items/default.png";
}

function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function positionModalToViewportCenter(modalId, dialogId) {
    return;
}
    

    function openRandomDraw() {
        drawRandom();
    }

    function openMyItemPage() {
        openMyItemModal();
    }

    let myItemCache = [];

    let currentItemName = "";
    let currentItemPrice = 0;

    function openItemModal(name, effect, desc, price, image) {
        document.getElementById("modalItemName").textContent = name;
        document.getElementById("modalItemEffect").textContent = effect;
        document.getElementById("modalItemDesc").textContent = desc;
        document.getElementById("modalItemPrice").textContent = price;
        document.getElementById("modalItemImage").src = image;
        document.getElementById("modalItemImage").alt = name;

        currentItemName = name;
        currentItemPrice = parseInt(price.replace(" COIN", "").replace(",", ""));

        const modal = document.getElementById("itemModal");
        const dialog = document.getElementById("itemModalDialog");

        modal.classList.remove("hidden");
        document.body.classList.add("modal-open");

        dialog.style.animation = "none";
        void dialog.offsetWidth;
        dialog.style.animation = "modalFadeUp 0.32s cubic-bezier(0.22, 1, 0.36, 1)";

        requestAnimationFrame(() => {
            positionModalToViewportCenter("itemModal", "itemModalDialog");
        });
    }

    function closeItemModal() {
        document.getElementById("itemModal").classList.add("hidden");
        document.body.classList.remove("modal-open");
    }

    function handleItemModalBackdrop(event) {
        if (event.target.id === "itemModal") {
            closeItemModal();
        }
    }

    function openMyItemModal() {
        const modal = document.getElementById("myItemModal");
        const dialog = document.getElementById("myItemModalDialog");
        const list = document.getElementById("myItemList");

        list.innerHTML = '<div class="text-slate-400 text-sm">불러오는 중...</div>';

        modal.classList.remove("hidden");
        document.body.classList.add("modal-open");

        dialog.style.animation = "none";
        void dialog.offsetWidth;
        dialog.style.animation = "modalFadeUp 0.32s cubic-bezier(0.22, 1, 0.36, 1)";

        requestAnimationFrame(() => {
            positionModalToViewportCenter("myItemModal", "myItemModalDialog");
        });

        loadMyItems();
    }
    window.addEventListener("resize", function() {
        if (!document.getElementById("itemModal").classList.contains("hidden")) {
            positionModalToViewportCenter("itemModal", "itemModalDialog");
        }
        if (!document.getElementById("myItemModal").classList.contains("hidden")) {
            positionModalToViewportCenter("myItemModal", "myItemModalDialog");
        }
    });

    function closeMyItemModal() {
        document.getElementById("myItemModal").classList.add("hidden");
        document.body.classList.remove("modal-open");
    }

    function handleMyItemModalBackdrop(event) {
        if (event.target.id === "myItemModal") {
            closeMyItemModal();
        }
    }

    function loadMyItems() {
        fetch("${pageContext.request.contextPath}/market/myItems", {
            method: "GET",
            headers: {
                "Accept": "application/json"
            }
        })
        .then(async res => {
            if (!res.ok) {
                throw new Error("HTTP " + res.status);
            }
            return res.json();
        })
        .then(items => {
            myItemCache = items || [];
            renderMyItems(myItemCache);
        })
        .catch(err => {
            console.error("내 아이템 조회 실패:", err);
            document.getElementById("myItemList").innerHTML =
                '<div class="my-item-empty">내 아이템을 불러오지 못했습니다.</div>';
        });
    }

    function renderMyItems(items) {
    const list = document.getElementById("myItemList");

    if (!items || items.length === 0) {
        list.innerHTML = '<div class="my-item-empty">보유한 아이템이 없습니다.</div>';
        resetMyItemPreview();
        return;
    }

    list.innerHTML = items.map(function(item, index) {
        const itemName = item.itemName || '이름 없음';
        const quantity = item.quantity || 1;
        const meta = getItemMeta(itemName);

        const imagePath = item.imagePath && item.imagePath.trim() !== ""
            ? item.imagePath
            : meta.image;

        const effect = item.itemEffect && item.itemEffect.trim() !== ""
            ? item.itemEffect
            : meta.effect;

        var safeName = escapeHtml(itemName);
        var safeEffect = escapeHtml(effect);
        var safeImg = escapeHtml(imagePath);
        var safeQty = escapeHtml(String(quantity));
        var idx = String(index);

        return '<div class="my-item-row" data-index="' + idx + '"' +
            ' onmouseenter="showMyItemPreview(myItemCache[' + idx + '])"' +
            ' onmouseleave="resetMyItemPreview()">' +
            '<img class="my-item-thumb" src="' + safeImg + '" alt="' + safeName + '"' +
            ' onerror="this.onerror=null;this.src=\'' + contextPath + '/images/items/default.png\'">' +
            '<div class="my-item-info">' +
            '<div class="my-item-name">' + safeName + '</div>' +
            '<div class="my-item-desc">' + safeEffect + '</div>' +
            '</div>' +
            '<div class="my-item-meta">x ' + safeQty + '</div>' +
            '</div>';
    }).join("");

    resetMyItemPreview();
}

    document.addEventListener("keydown", function(e) {
        if (e.key === "Escape") {
            closeItemModal();
            closeMyItemModal();
        }
    });

    function bindShopBuyButtons() {
        document.querySelectorAll('.buy-btn[data-item-name]').forEach(function(btn){
            if (btn.dataset.bound === '1') return;
            btn.dataset.bound = '1';
            btn.addEventListener('click', function(e){
                e.preventDefault();
                e.stopPropagation();
                openItemModal(
                    btn.dataset.itemName || '',
                    btn.dataset.itemEffect || '',
                    btn.dataset.itemDesc || '',
                    btn.dataset.itemPrice || '',
                    btn.dataset.itemImage || ''
                );
            });
        });
    }

    bindShopBuyButtons();

    const filterButtons = document.querySelectorAll(".filter-btn");
    const shopItems = document.querySelectorAll(".shop-item");

    filterButtons.forEach(function(button) {
        button.addEventListener("click", function() {
            const filter = this.dataset.filter;

            shopItems.forEach(function(item) {
                const category = item.dataset.category;

                if (filter === "all" || category === filter) {
                    item.classList.remove("hidden");
                } else {
                    item.classList.add("hidden");
                }
            });

            filterButtons.forEach(function(btn) {
                btn.classList.remove("is-active");
            });

            this.classList.add("is-active");
        });
    });

    function buyCurrentItem() {
        buyItem(currentItemName, currentItemPrice);
    }

    // 카카오페이
    function buyItem(itemName, price) {
    const confirmBuy = confirm(price + " COIN으로 '" + itemName + "'을 구매하시겠습니까?");

    if (!confirmBuy) {
        return;
    }

    fetch("${pageContext.request.contextPath}/market/buyItem", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            itemName: itemName,
            price: price
        })
    })
    .then(async res => {
        const raw = await res.text();

        if (!res.ok) {
            console.error("buyItem 응답 오류:", raw);
            throw new Error("HTTP " + res.status);
        }

        let data;

        try {
            data = JSON.parse(raw);
        } catch (e) {
            const text = (raw || "").trim();

            if (text === "success" || text === "lack" || text === "logout" || text === "error") {
                data = { result: text };
            } else {
                console.error("JSON 파싱 실패, raw 응답:", raw);
                throw new Error("응답 형식 오류");
            }
        }

        return data;
    })
    .then(data => {
        const coinText = document.getElementById("currentCoinText");
        const currentCoin = coinText ? parseInt(coinText.textContent.replace(/,/g, ""), 10) || 0 : 0;

        if (data.result === "success") {
            alert(itemName + " 구매가 완료되었습니다.");
            closeItemModal();

            if (coinText) {
                if (data.currentCoin !== undefined && data.currentCoin !== null) {
                    coinText.textContent = data.currentCoin;
                } else {
                    coinText.textContent = Math.max(0, currentCoin - price);
                }
            }

            loadMyItems();
        } else if (data.result === "lack") {
            alert("코인이 부족합니다.");
        } else if (data.result === "logout") {
            alert("로그인 후 이용 가능합니다.");

            // 현재 페이지 기억해서 로그인 후 다시 돌아오게
            const currentUrl = window.location.pathname + window.location.search;

            location.href = "${pageContext.request.contextPath}/login?redirect=" + encodeURIComponent(currentUrl);
        } else {
            alert("구매 실패");
            console.error("buyItem 알 수 없는 응답:", data);
        }
    })
    .catch(err => {
        console.error("fetch error:", err);
        alert("서버 요청 실패: " + err.message);
    });
}

// KakaoPay
document.addEventListener("DOMContentLoaded", function () {
    const coinChargeBtn = document.getElementById("coinChargeBtn");
    const kakaoPayModal = document.getElementById("kakaoPayModal");
    const kakaoCloseBtn = document.getElementById("kakaoCloseBtn");
    const kakaoPayBtn = document.getElementById("kakaoPayBtn");
    const chargeItems = document.querySelectorAll(".charge-item");

    let selectedAmount = 0;

    // 충전 모달 열기
    if (coinChargeBtn && kakaoPayModal) {
        coinChargeBtn.addEventListener("click", function () {
            kakaoPayModal.classList.add("show");
        });
    }

    // 충전 모달 닫기
    if (kakaoCloseBtn && kakaoPayModal) {
        kakaoCloseBtn.addEventListener("click", function () {
            kakaoPayModal.classList.remove("show");
        });
    }

    // 금액 선택
    chargeItems.forEach(function (item) {
        item.addEventListener("click", function () {
            chargeItems.forEach(function (i) {
                i.classList.remove("active");
            });
            this.classList.add("active");
            selectedAmount = Number(this.dataset.amount);
            console.log("선택 금액:", selectedAmount);
        });
    });

    // 카카오페이 결제 버튼
    if (kakaoPayBtn) {
        kakaoPayBtn.addEventListener("click", function () {
            if (!selectedAmount) {
                alert("금액을 선택하세요.");
                return;
            }

            console.log("카카오 ready 요청 시작:", selectedAmount);

            fetch("${pageContext.request.contextPath}/kakao/ready", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    amount: selectedAmount
                })
            })
            .then(async function (res) {
                const text = await res.text();
                console.log("ready 응답 원문:", text);

                if (!res.ok) {
                    throw new Error("HTTP " + res.status + " / " + text);
                }

                return JSON.parse(text);
            })
            .then(function (data) {
                console.log("ready 응답 JSON:", data);

                if (data.result === "logout") {
                    alert("로그인이 필요합니다.");
                    const currentUrl = window.location.pathname + window.location.search;
                    location.href = "${pageContext.request.contextPath}/login?redirect=" + encodeURIComponent(currentUrl);
                    return;
                }

                if (data.result === "success" && data.redirectUrl) {
                    location.href = data.redirectUrl;
                    return;
                }

                alert("결제 준비 중 오류가 발생했습니다.");
            })
            .catch(function (err) {
                console.error("카카오페이 요청 오류:", err);
                alert("서버 요청 중 오류가 발생했습니다.\n콘솔(F12)을 확인하세요.");
            });
        });
    }

    /* 메인 등에서 #shop-item-* 로 진입 시 해당 카드로 스크롤 */
    var hash = window.location.hash;
    if (hash && hash.indexOf("shop-item-") === 1) {
        window.setTimeout(function () {
            var el = document.querySelector(hash);
            if (el) {
                el.scrollIntoView({ behavior: "smooth", block: "center" });
                el.classList.add("shop-item--hash-focus");
                window.setTimeout(function () {
                    el.classList.remove("shop-item--hash-focus");
                }, 2200);
            }
        }, 120);
    }
});

</script>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

<!-- 카카오페이 -->
<!-- 카카오페이 충전 모달 -->
<div id="kakaoPayModal" class="kakao-modal">
    <div class="kakao-modal-content">
        <div class="kakao-modal-header">
            <span>코인 충전</span>
            <button type="button" id="kakaoCloseBtn">&times;</button>
        </div>

        <div class="kakao-modal-body">
            <p>충전할 금액을 선택하세요</p>

            <div class="charge-list">
                <button type="button" class="charge-item" data-amount="1000">1,000 코인</button>
                <button type="button" class="charge-item" data-amount="5000">5,000 코인</button>
                <button type="button" class="charge-item" data-amount="10000">10,000 코인</button>
                <button type="button" class="charge-item" data-amount="100000">100,000 코인</button>
            </div>

            <button type="button" id="kakaoPayBtn" class="kakao-pay-btn">
                카카오페이 결제하기
            </button>
        </div>
    </div>
</div>

</body>
</html>