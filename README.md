# See config.yml for stage formats; GitHub Actions workflow is under .github/workflows/main.yml


## v1.2.0 추가사항
- **스테이지별 보상 GUI**: `/생존디펜스 보상설정 <번호>` 또는 `/sdef rewardgui <stage>`로 GUI를 열고, 아이템을 넣은 뒤 **창을 닫으면 저장**됩니다. GUI에 넣은 아이템은 **자동으로 돌려받습니다**(복사본만 저장).
- **스테이지 제한시간**: 기본 `stage_time.default_sec`(0=비활성화) + `/생존디펜스 제한시간 <번호> <초>`로 per-stage 설정. 시간 초과 시 **게임 종료**.
