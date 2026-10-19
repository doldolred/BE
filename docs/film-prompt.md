# 메인 스크롤 필름 — 영상 생성 프롬프트 (Gemini / Veo)

홈 히어로 뒤에서 스크롤에 맞춰 넘어가는 10초 영상. `FE/src/data/story.ts` 의 `FILM.frames` 세 구간과 맞춘다.

| 구간 | 시간 | 화면 | 겹쳐지는 문장 |
| --- | --- | --- | --- |
| THE ROOT | 0~3초 | 흙 묻은 비트 원물 | 흙 속에서 자란 붉은 뿌리 |
| THE PRESS | 3~7초 | 착즙 — 붉은 즙이 흘러내림 | 통째로, 그대로 짜냅니다 |
| THE POUCH | 7~10초 | 즙이 파우치에 담기고, 완성된 파우치가 멈춰 선다 | 하루 한 포의 붉은 습관 |

## 영상이 지켜야 할 것 (스크롤 필름용)

- **컷 없이 한 흐름으로.** 스크롤로 앞뒤로 되감기 때문에 장면 전환(컷)이 있으면 끊겨 보인다. 카메라가 천천히 이동·줌하며 자연스럽게 이어져야 한다.
- **글자 자리 비워 두기.** 표제·문장이 **왼쪽 아래**에 올라간다. 피사체는 화면 **오른쪽~가운데**, 왼쪽 아래는 어둡고 단순하게.
- **어두운 바탕.** 사이트 배경이 짙은 자주빛 검정(#140a0e)이라 영상 가장자리가 어두워야 자연스럽게 섞인다.
- **마지막 장면은 정지에 가깝게.** 마지막 프레임이 포스터(첫 화면 · 움직임 줄임 설정)로도 쓰인다. 완성된 파우치가 또렷하게 멈춰 있어야 한다.
- 16:9 가로, 10초, 글자·로고·자막 없이(파우치에 인쇄된 것 제외).

## 프롬프트 (영어 — 그대로 붙여 넣기)

파우치 사진을 참조 이미지로 같이 올리고, 아래를 프롬프트로.

```
A single continuous 10-second cinematic product film, no cuts, 16:9, dark moody studio.

Background: deep burgundy-black (#140a0e) seamless backdrop with soft falloff to pure black at the frame edges. One warm key light from the upper right, subtle rim light. Lower-left quarter of the frame stays dark and empty throughout (text will be overlaid there). Subject sits in the right two-thirds of the frame.

0–3s: Extreme close-up of a whole raw beetroot with dark soil still clinging to its skin and thin roots, resting on a dark slate surface. Slow dolly-in. A few grains of soil fall off. Water droplets glisten on the deep crimson skin.

3–7s: The camera glides smoothly to the right as the beet is cut, revealing its concentric rings; a cold press squeezes it and thick, glossy crimson beet juice streams down in slow motion, catching the light. No hands or people visible, or only a hint of a hand in shadow.

7–10s: The juice stream flows down and fills a transparent-windowed stick pouch — match the pouch design exactly from the reference image (shape, color, printed label). The camera settles; the filled pouch stands upright, dewy and softly lit, centered right, and holds perfectly still for the final second.

Style: premium beverage commercial, macro photography, shallow depth of field, slow and deliberate motion, 24fps look, rich crimson and magenta tones against black, slight film grain. No text, no logos except what is printed on the pouch, no people, no watermark, no cuts, no camera shake.
```

## 짧은 버전 (글자 수 제한이 있을 때)

```
Continuous 10s cinematic macro shot, no cuts, 16:9, dark burgundy-black studio background, subject on the right, lower-left kept dark. A soil-covered raw beetroot on slate → slow dolly as it is cut and cold-pressed, glossy crimson juice streaming in slow motion → the juice fills the stick pouch from the reference image, which settles upright and holds still. Premium beverage commercial, shallow depth of field, rich crimson tones, film grain. No text, no people, no camera shake.
```

## 참조 이미지(파우치)와 같이 줄 때 한 줄

```
Use the attached image as the exact product: keep its shape, colors and printed label unchanged in the final shot.
```

## 받은 mp4 를 사이트에 넣는 법

```bash
# 1초에 12장, 가로 1280, WebP 로 뽑기 (10초 → 120장, 약 3MB)
ffmpeg -i film.mp4 -vf "fps=12,scale=1280:-2" -c:v libwebp -quality 62 FE/public/film/f%03d.webp
# 마지막 장을 포스터로
cp FE/public/film/f120.webp FE/public/film/poster.webp
```

그리고 `FE/src/data/story.ts` 에서:

```ts
sequence: { base: '/film/f', pad: 3, ext: '.webp', count: 120 },
poster: '/film/poster.webp',
```

`count` 는 실제로 뽑힌 파일 수와 같아야 한다.

---

# 제품 360° 턴테이블 — 영상 생성 프롬프트 (Gemini / Veo)

상품 상세 · 홈 라인업에서 **끌어서 돌려 보는** 제품 사진 묶음. 제품 하나당 영상 하나. 파우치 사진을 참조 이미지로 같이 올린다.

## 지켜야 할 것

- **카메라 고정, 제품만 제자리에서 한 바퀴.** 정확히 360° 한 바퀴를 돌고 처음 자세로 돌아와야 끊김 없이 이어진다.
- **일정한 속도.** 빨라졌다 느려지면 드래그 감각이 어긋난다.
- **배경은 사이트 바탕색 #140a0e 단색.** 그라데이션 · 바닥 반사 없이. 그래야 무대와 경계가 안 보인다.
- 제품이 화면 가운데, 위아래로 꽉 차지 않게(높이의 80% 정도). 조명은 고정.
- 16:9 · 8초 · 글자 · 손 · 그림자 움직임 없음.

## 프롬프트

```
Product turntable shot, single continuous take, no cuts, 16:9, 8 seconds.

The product from the reference image — keep its shape, colors and printed label exactly — stands upright in the center of the frame on an invisible surface and rotates smoothly around its vertical axis, exactly one full 360-degree turn at a perfectly constant speed, ending in the same pose it started in.

Camera locked, slightly above eye level, no zoom, no movement. Background: solid flat #140a0e (very dark burgundy-black), no gradient, no floor reflection, no vignette. One soft key light from the upper left and a subtle rim light, both fixed. The product fills about 80% of the frame height with empty space above and below.

Premium beverage commercial look, sharp focus across the whole product, no text, no hands, no particles, no watermark.
```

참조 이미지에 붙일 한 줄:

```
Use the attached image as the exact product. Keep the label, colors and proportions unchanged from every angle.
```

## 받은 mp4 를 사이트에 넣는 법

```bash
# 한 바퀴 8초 → 1초에 9장 = 72장. 폴더 이름은 제품별로 (beet, extract, powder …)
ffmpeg -i turntable.mp4 -t 8 -vf "fps=9,scale=1000:-2" -c:v libwebp -quality 70 FE/public/showcase/beet/t%03d.webp
```

그리고 `FE/src/data/showcase.ts` 에서 그 제품 항목의 `turntable` 을:

```ts
turntable: { base: '/showcase/beet/t', pad: 3, ext: '.webp', count: 72 },
```

`count` 는 뽑힌 파일 수와 같아야 한다. 영상이 제자리로 정확히 안 돌아오면 마지막 몇 장을 지우고 `count` 를 줄인다.

제품이 늘면(농축액 · 파우더) 같은 방식으로 영상 하나씩 만들어 `showcase.ts` 에 항목을 더한다. `features` 의 `at` 은 그 특징이 켜질 각도(0 정면 · 0.5 뒷면)라 라벨 위치에 맞춰 잡는다.
