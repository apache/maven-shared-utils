/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.utils.introspection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 *
 */
public class ReflectionValueExtractorTest extends TestCase {
    private Project project;

    protected void setUp() throws Exception {
        super.setUp();

        Dependency dependency1 = new Dependency();
        dependency1.setArtifactId("dep1");
        Dependency dependency2 = new Dependency();
        dependency2.setArtifactId("dep2");

        project = new Project();
        project.setModelVersion("4.0.0");
        project.setGroupId("org.apache.maven");
        project.setArtifactId("maven-core");
        project.setName("Maven");
        project.setVersion("2.0-SNAPSHOT");
        project.setScm(new Scm());
        project.getScm().setConnection("scm-connection");
        project.addDependency(dependency1);
        project.addDependency(dependency2);
        project.setBuild(new Build());

        // Build up an artifactMap
        project.addArtifact(new Artifact("g0", "a0", "v0", "e0", "c0"));
        project.addArtifact(new Artifact("g1", "a1", "v1", "e1", "c1"));
        project.addArtifact(new Artifact("g2", "a2", "v2", "e2", "c2"));
    }

    public void testValueExtraction() throws IntrospectionException {
        // ----------------------------------------------------------------------
        // Top level values
        // ----------------------------------------------------------------------

        assertEquals("4.0.0", ReflectionValueExtractor.evaluate("project.modelVersion", project));

        assertEquals("org.apache.maven", ReflectionValueExtractor.evaluate("project.groupId", project));

        assertEquals("maven-core", ReflectionValueExtractor.evaluate("project.artifactId", project));

        assertEquals("Maven", ReflectionValueExtractor.evaluate("project.name", project));

        assertEquals("2.0-SNAPSHOT", ReflectionValueExtractor.evaluate("project.version", project));

        // ----------------------------------------------------------------------
        // SCM
        // ----------------------------------------------------------------------

        assertEquals("scm-connection", ReflectionValueExtractor.evaluate("project.scm.connection", project));

        // ----------------------------------------------------------------------
        // Dependencies
        // ----------------------------------------------------------------------

        List<?> dependencies = (List<?>) ReflectionValueExtractor.evaluate("project.dependencies", project);

        assertNotNull(dependencies);

        assertEquals(2, dependencies.size());

        // ----------------------------------------------------------------------
        // Dependencies - using index notation
        // ----------------------------------------------------------------------

        // List
        Dependency dependency = (Dependency) ReflectionValueExtractor.evaluate("project.dependencies[0]", project);

        assertNotNull(dependency);

        assertEquals("dep1", dependency.getArtifactId());

        String artifactId = (String) ReflectionValueExtractor.evaluate("project.dependencies[1].artifactId", project);

        assertEquals("dep2", artifactId);

        // Array

        dependency = (Dependency) ReflectionValueExtractor.evaluate("project.dependenciesAsArray[0]", project);

        assertNotNull(dependency);

        assertEquals("dep1", dependency.getArtifactId());

        artifactId = (String) ReflectionValueExtractor.evaluate("project.dependenciesAsArray[1].artifactId", project);

        assertEquals("dep2", artifactId);

        // Map

        dependency = (Dependency) ReflectionValueExtractor.evaluate("project.dependenciesAsMap(dep1)", project);

        assertNotNull(dependency);

        assertEquals("dep1", dependency.getArtifactId());

        artifactId = (String) ReflectionValueExtractor.evaluate("project.dependenciesAsMap(dep2).artifactId", project);

        assertEquals("dep2", artifactId);

        // ----------------------------------------------------------------------
        // Build
        // ----------------------------------------------------------------------

        Build build = (Build) ReflectionValueExtractor.evaluate("project.build", project);

        assertNotNull(build);
    }

    public void testValueExtractorWithAInvalidExpression() throws IntrospectionException {
        assertNull(ReflectionValueExtractor.evaluate("project.foo", project));
        assertNull(ReflectionValueExtractor.evaluate("project.dependencies[10]", project));
        assertNull(ReflectionValueExtractor.evaluate("project.dependencies[0].foo", project));
    }

    public void testMappedDottedKey() throws IntrospectionException {
        Map<String, String> map = new HashMap<>();
        map.put("a.b", "a.b-value");

        assertEquals("a.b-value", ReflectionValueExtractor.evaluate("h.value(a.b)", new ValueHolder(map)));
    }

    public void testIndexedMapped() throws IntrospectionException {
        Map<Object, Object> map = new HashMap<>();
        map.put("a", "a-value");
        List<Object> list = new ArrayList<>();
        list.add(map);

        assertEquals("a-value", ReflectionValueExtractor.evaluate("h.value[0](a)", new ValueHolder(list)));
    }

    public void testMappedIndexed() throws IntrospectionException {
        List<Object> list = new ArrayList<>();
        list.add("a-value");
        Map<Object, Object> map = new HashMap<>();
        map.put("a", list);
        assertEquals("a-value", ReflectionValueExtractor.evaluate("h.value(a)[0]", new ValueHolder(map)));
    }

    public void testMappedMissingDot() throws IntrospectionException {
        Map<Object, Object> map = new HashMap<>();
        map.put("a", new ValueHolder("a-value"));
        assertNull(ReflectionValueExtractor.evaluate("h.value(a)value", new ValueHolder(map)));
    }

    public void testIndexedMissingDot() throws IntrospectionException {
        List<Object> list = new ArrayList<>();
        list.add(new ValueHolder("a-value"));
        assertNull(ReflectionValueExtractor.evaluate("h.value[0]value", new ValueHolder(list)));
    }

    public void testDotDot() throws IntrospectionException {
        assertNull(ReflectionValueExtractor.evaluate("h..value", new ValueHolder("value")));
    }

    public void testBadIndexedSyntax() throws IntrospectionException {
        List<Object> list = new ArrayList<>();
        list.add("a-value");
        Object value = new ValueHolder(list);

        assertNull(ReflectionValueExtractor.evaluate("h.value[", value));
        assertNull(ReflectionValueExtractor.evaluate("h.value[]", value));
        assertNull(ReflectionValueExtractor.evaluate("h.value[a]", value));
        assertNull(ReflectionValueExtractor.evaluate("h.value[0", value));
        assertNull(ReflectionValueExtractor.evaluate("h.value[0)", value));
        assertNull(ReflectionValueExtractor.evaluate("h.value[-1]", value));
    }

    public void testBadMappedSyntax() throws IntrospectionException {
        Map<Object, Object> map = new HashMap<>();
        map.put("a", "a-value");
        Object value = new ValueHolder(map);

        assertNull(ReflectionValueExtractor.evaluate("h.value(", value));
        assertNull(ReflectionValueExtractor.evaluate("h.value()", value));
        assertNull(ReflectionValueExtractor.evaluate("h.value(a", value));
        assertNull(ReflectionValueExtractor.evaluate("h.value(a]", value));
    }

    public void testIllegalIndexedType() {
        try {
            ReflectionValueExtractor.evaluate("h.value[1]", new ValueHolder("string"));
        } catch (IntrospectionException e) {
            assertNotNull(e.getMessage());
        }
    }

    public void testIllegalMappedType() {
        try {
            ReflectionValueExtractor.evaluate("h.value(key)", new ValueHolder("string"));
        } catch (IntrospectionException e) {
            assertNotNull(e.getMessage());
        }
    }

    public void testTrimRootToken() throws IntrospectionException {
        assertNull(ReflectionValueExtractor.evaluate("project", project, true));
    }

    public void testArtifactMap() throws IntrospectionException {
        assertEquals(
                "g0",
                ((Artifact) ReflectionValueExtractor.evaluate("project.artifactMap(g0:a0:c0)", project)).getGroupId());
        assertEquals(
                "a1",
                ((Artifact) ReflectionValueExtractor.evaluate("project.artifactMap(g1:a1:c1)", project))
                        .getArtifactId());
        assertEquals(
                "c2",
                ((Artifact) ReflectionValueExtractor.evaluate("project.artifactMap(g2:a2:c2)", project))
                        .getClassifier());
    }

    public static class Artifact {
        private String groupId;
        private String artifactId;
        private String version;
        private String extension;
        private String classifier;

        public Artifact(String groupId, String artifactId, String version, String extension, String classifier) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.extension = extension;
            this.classifier = classifier;
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getClassifier() {
            return classifier;
        }

        public void setClassifier(String classifier) {
            this.classifier = classifier;
        }
    }

    public static class Project {
        private String modelVersion;

        private String groupId;

        private Scm scm;

        private final List<Dependency> dependencies = new ArrayList<>();

        private Build build;

        private String artifactId;

        private String name;

        private String version;

        private Map<String, Artifact> artifactMap = new HashMap<>();

        private String description;

        public void setModelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public void setScm(Scm scm) {
            this.scm = scm;
        }

        public void addDependency(Dependency dependency) {
            this.dependencies.add(dependency);
        }

        public void setBuild(Build build) {
            this.build = build;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Scm getScm() {
            return scm;
        }

        public String getModelVersion() {
            return modelVersion;
        }

        public String getGroupId() {
            return groupId;
        }

        public List<Dependency> getDependencies() {
            return dependencies;
        }

        public Build getBuild() {
            return build;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public Dependency[] getDependenciesAsArray() {
            List<Dependency> list = getDependencies();
            return list.toArray(new Dependency[list.size()]);
        }

        public Map<String, Dependency> getDependenciesAsMap() {
            Map<String, Dependency> ret = new HashMap<>();
            for (Dependency dep : getDependencies()) {
                ret.put(dep.getArtifactId(), dep);
            }
            return ret;
        }

        // ${project.artifactMap(g:a:v)}
        public void addArtifact(Artifact a) {
            artifactMap.put(a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getClassifier(), a);
        }

        public Map<String, Artifact> getArtifactMap() {
            return artifactMap;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Build {}

    public static class Dependency {
        private String artifactId;

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String id) {
            artifactId = id;
        }
    }

    public static class Scm {
        private String connection;

        public void setConnection(String connection) {
            this.connection = connection;
        }

        public String getConnection() {
            return connection;
        }
    }

    public static class ValueHolder {
        private final Object value;

        public ValueHolder(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    public void testRootPropertyRegression() throws IntrospectionException {
        Project project = new Project();
        project.setDescription("c:\\\\org\\apache\\test");
        Object evalued = ReflectionValueExtractor.evaluate("description", project);
        assertNotNull(evalued);
    }
}
