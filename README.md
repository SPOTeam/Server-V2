# 📚 스터디 매칭 및 관리 플랫폼, 스팟

> **기간**: 2025.09 - 진행 중
> 

> **팀 구성**: P.O 1명, 디자인 1명, AOS 2명, 백엔드 1명
> 

> **역할**: 백엔드 개발, 인프라 관리 (기여도 100%)
> 

---

<img width="100" height="100" alt="Logo" src="https://github.com/user-attachments/assets/a81aef66-058a-4f9b-bc60-ed0901a01d7e" />


---

# 📝 프로젝트 개요

- 스터디 매칭 및 관리 플랫폼
- 현재 Ver.1 출시 완료, Ver.2 개발 진행 중

---

# 🛠 주요 기술 스택 및 아키텍처

### 모듈러 모놀리식 (Modular Monolith) 아키텍처

**▼ 전체 아키텍처 개요**

<img width="3412" height="1792" alt="image" src="https://github.com/user-attachments/assets/7d9d7c64-37b2-469e-b0eb-de32c54f3ab6" />


**▼ 모듈 간 소통 방법**

<img width="3034" height="360" alt="image" src="https://github.com/user-attachments/assets/d78cb376-0d88-4d79-9ea5-801f8d8e30a4" />


- 도메인 격리:
    - 모듈 간 클래스/엔티티 직접 참조 금지
- DB 격리:
    - 도메인 간 JOIN 금지, 타 도메인 엔티티는 ID(PK)로만 간접 참조
 
---

### 인프라 아키텍처 다이어그램
<img width="672" height="686" alt="스크린샷 2025-12-26 오후 4 43 29" src="https://github.com/user-attachments/assets/b29cedca-5a0e-493c-afe9-fafa5b20ae1b" />

