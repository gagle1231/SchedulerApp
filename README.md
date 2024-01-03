# SchedulerApp

### 모바일 응용 개인 프로젝트  
#### 

## 프로젝트 메뉴얼

- [앱 개요](#app)
- [사용 API](#api)
- [개선할 점](#feedback)




> ### app

1. 앱의 목적 및 사용 용도
  * 모든 약속이나 일정을 앱으로 쉽게 관리
  * 하나의 약속에 대한 상세 정보를 저장
  * 약속 시간 전 알람


2. 앱의 기능
  * 전체 약속 리스트 보기
  * 오늘 약속 리스트 보기
  * 오늘 날씨 보기(날씨 API 사용, 위치 정보 사용)
  * 약속 추가하기, 상세보기, 수정하기, 삭제하기
  * 약속 전 알람(Alarm Service 이용)
  * 장소 검색 기능(구글맵, Google Places API, GeoCoding 사용)
  * 트위터 공유 기능

  
> ### API
* 날씨 API: 공공데이터포털(https://www.data.go.kr/iim/api/selectAPIAcountView.do) 기상청_단기예보 ((구)_동네예보) 조회서비스

-> XML형식으로 데이터를 받아서 Parser 구현하여 사용

->현재 위치 정보를 얻어서 날씨 API를 이용하여 현재 위치의 날씨 정보 조회


* 지도: Google Map API, Google Places API

-> 위치 검색 기능, 주변 음식점 추천





> ### feedback
* Retrofit 이용해보기
* UI 변경
* 알람 기능 사용자에게 입력받는 것으로 수정
