Apache Maven Shared Utils

This project aims to be a functional replacement for
plexus-utils in maven core. It is not a 100% API compatible
replacement though. Lots of methods got cleaned up, generics
got added and we dropped a lot of unused code.


Relation to Commons-*

maven-shared-utils internally use commons-io. We shade all commons
classes into our own private package to prevent classpatch clashes.
This is the reason why any public API in maven-shared-utils must
avoid to expose commons classe directly. Most times it's sufficient
to just create an empty subclass and expose that instead.
