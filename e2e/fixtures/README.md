# E2E 테스트 사진

역할 ②가 넣는다(docs/roles.md). spec이 아래 이름을 그대로 쓴다. 긴 변이 1400px 이상인 사진은 올릴 때 OPT-3으로 줄어든다.

| 파일 | 내용 | 크기 | 쓰는 흐름 | 상태 |
| --- | --- | --- | --- | --- |
| `menu-board.jpg` | 설렁탕·콩나물국·그린샐러드·버섯리조또가 적힌 칠판 메뉴판(가격 포함, 그려서 만듦) | 2000×1500 | `scan-menu-board` | 있음 |
| `seolleongtang.jpg` | 설렁탕 음식 사진 | | `scan-food-photo-single-dish`, `diet-toggle-only` | **없음** TODO(확인) |
| `no-menu.jpg` | 메뉴·음식이 없는 풍경 그림 | 1200×900 | `manual-input-fallback`, `manual-input-unknown-menu` | 있음 |
| `bibimbap.jpg` | 비빔밥 음식 사진 | | `halal-bibimbap-gochujang` | **없음** TODO(확인) |
| `bibimguksu.jpg` | 비빔국수 음식 사진 | | `peanut-allergy-bibimguksu` | **없음** TODO(확인) |

흐름에는 아직 쓰지 않는 음식 사진이다. 팀원이 준 사진을 JPEG로 바꿨고, 모두 1400px 미만이라 줄이지 않고 올라간다.

| 파일 | 내용 | 크기 |
| --- | --- | --- |
| `kimchi-jjigae.jpg` | 돼지고기 김치찌개(냄비) | 554×554 |
| `jeyuk-bokkeum.jpg` | 제육볶음(철판, 사진 위에 글자 워터마크 있음) | 1000×961 |
| `curry-rice.jpg` | 카레라이스 | 773×1031 |

- 김치찌개·제육볶음은 돼지고기, 카레라이스는 고기가 보여서 할랄·채식 판정을 사진으로 확인할 때 쓸 수 있다. 흐름에 쓰려면 음식 시드(③ `R__30`)에 넣어야 한다.
- `jeyuk-bokkeum.jpg`·`curry-rice.jpg`에는 원래 올린 사람의 워터마크가 있다. 저장소 밖(발표 자료·배포)에 쓰기 전에 출처를 확인한다.
