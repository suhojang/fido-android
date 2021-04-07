### fido-android

+ intro
  + Andorid Face인증, 지문인증을 google GMS, google guava를 사용하여 구현 하였습니다.
  + 과거 Andorid에는 Face인증, 지문인증을 별도로 구축이 필요하여 개발하게 되었습니다.
  + 오픈소스를 사용하다보니 Face의 좌표를 어떻게 구분하는지와 지문인식을 하기위한 프로세스들을 이해 할 수 있었습니다.
  + 지문인식 같은경우 자체 local repository에 지문 data를 저장하고, PKI기술을 이용하여 검증을 한다는 것을 알게 되었습니다.

```
git clone https://github.com/suhojang/fido-android.git
```
