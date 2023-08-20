# 우리들의 코딩 경로 Cours = code + ours


<a href="https://play.google.com/store/apps/details?id=com.pjm.cours">
    <img src="http://github.com/PARKJONGMlN/Cours/assets/77707692/ab98934b-b749-4e67-9175-a9eb7faea3d6" width="250" height="100"/>
</a>

> **개발자 스터디 커뮤니티 ‘코스’입니다.**

- 원하는 스터디를 생성하고 가입 할 수 있습니다.
- 주변의 스터디를 찾을 수 있습니다.
- 관심있는 언어, 카테고리를 고를 수 있습니다.
- 함께하는 스터디원과 채팅을 할 수 있습니다.

## **주요 기능 소개**
## **기술 스택**
- Minimum SDK level 24
- [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous.
- JetPack
  - [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) - Create a UI that automatically responds to lifecycle events.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Build data objects that notify views when the underlying database changes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Store UI related data that isn't destroyed on app rotations.
  - [Room](https://developer.android.com/training/data-storage/room) - Constructs Database by providing an abstraction layer over SQLite to allow fluent database access.
  - [DataBinding](https://developer.android.com/topic/libraries/data-binding) - Useful to bind data directly through layouts xml file, so no `findViewById()` anymore.
  - [Navigation](https://developer.android.com/guide/navigation) - Handles navigating between your app's destinations.
- [Hilt](https://dagger.dev/hilt/) - Dependency injection.
- [Coil](https://coil-kt.github.io/coil/) - An image loading library for Android backed by Kotlin Coroutines.
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit) - Construct the REST APIs.
- [Firebase](https://firebase.google.com/)
  - Realtime DB
  - Storage
  - Auth
  - FCM

## Architecture
Cours 는 MVVM 아키텍처와 Repository 패턴을 적용했습니다.
<p align = 'center'>
<img width = '600' src = 'https://user-images.githubusercontent.com/39554623/184456867-195f5989-dc9a-4dea-8f35-41e1f11145ff.png'>
</p>
