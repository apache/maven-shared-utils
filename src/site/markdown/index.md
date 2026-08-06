<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Apache Maven Shared Utils

This project replaces [plexus-utils](https://codehaus-plexus.github.io/plexus-utils/) for Maven.

It is not a 100% API compatible replacement. We cleaned up methods, added generics, and removed unused code.

This project adds new features, like [styled message API](./apidocs/org/apache/maven/shared/utils/logging/package-summary.html).

# Why?

plexus-utils consisted mostly of code that originated in various Apache projects. Maven Shared Utils is based on those original Apache-source implementations.

# Why not commons?

We prefer code to use commons-* where appropriate. The plexus-utils versions became incompatible with the Commons versions over time. Migrating is not always a 1:1 operation. Migrating to Maven Shared Utils works in most cases as a 1:1 operation.
