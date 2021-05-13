/**
 * Copyright © 2010-2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.rules;

import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.RuleLogger;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.util.NameHelper;
import org.jsonschema2pojo.util.ParcelableHelper;
import org.jsonschema2pojo.util.ReflectionHelper;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

/**
 * Provides factory/creation methods for the code generation rules.
 */
public class RuleFactory {

    private RuleLogger logger;
    private NameHelper nameHelper;
    private ReflectionHelper reflectionHelper;
    private GenerationConfig generationConfig;
    private Annotator annotator;
    private SchemaStore schemaStore;

    /**
     * Create a new rule factory with the given generation config options.
     *
     * @param generationConfig
     *            The generation config options for type generation. These
     *            config options will influence the java code generated by rules
     *            created by this factory.
     * @param annotator
     *            the annotator used to mark up Java types with any annotations
     *            that are required to build JSON compatible types
     * @param schemaStore
     *            the object used by this factory to get and store schemas
     */
    public RuleFactory(GenerationConfig generationConfig, Annotator annotator, SchemaStore schemaStore) {
        this.generationConfig = generationConfig;
        this.annotator = annotator;
        this.schemaStore = schemaStore;
        this.nameHelper = new NameHelper(generationConfig);
        this.reflectionHelper = new ReflectionHelper(this);
    }

    /**
     * Create a rule factory with the default generation config options.
     *
     * @see DefaultGenerationConfig
     */
    public RuleFactory() {
        this(new DefaultGenerationConfig(), new Jackson2Annotator(new DefaultGenerationConfig()), new SchemaStore());
    }

    /**
     * Provides a rule instance that should be applied when an "array"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "array" declaration.
     */
    public Rule<JPackage, JClass> getArrayRule() {
        return new ArrayRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a "description"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "description" declaration.
     */
    public Rule<JDocCommentable, JDocComment> getDescriptionRule() {
        return new DescriptionRule();
    }

    /**
     * Provides a rule instance that should be applied when a "$comment"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "$comment" declaration.
     */
    public Rule<JDocCommentable, JDocComment> getCommentRule() {
        return new CommentRule();
    }

    /**
     * Provides a rule instance that should be applied when an "enum"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "enum" declaration.
     */
    public Rule<JClassContainer, JType> getEnumRule() {
        return new EnumRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a "format"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "format" declaration.
     */
    public Rule<JType, JType> getFormatRule() {
        return new FormatRule(this);
    }

    /**
     * Provides a rule instance that should be applied when an "object"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "object" declaration.
     */
    public Rule<JPackage, JType> getObjectRule() {
        return new ObjectRule(this, new ParcelableHelper(), reflectionHelper);
    }

    /**
     * Provides a rule instance that should be applied to add constructors to a generated type
     *
     * @return a schema rule that can handle the "object" declaration.
     */
    public Rule<JDefinedClass, JDefinedClass> getConstructorRule()
    {
        return new ConstructorRule(this, reflectionHelper);
    }

    /**
     * Provides a rule instance that should be applied when a "required"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "required" declaration.
     */
    public Rule<JDefinedClass, JDefinedClass> getRequiredArrayRule() { return new RequiredArrayRule(this); }

    /**
     * Provides a rule instance that should be applied when a "properties"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "properties" declaration.
     */
    public Rule<JDefinedClass, JDefinedClass> getPropertiesRule() {
        return new PropertiesRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a property
     * declaration (child of the "properties" declaration) is found in the
     * schema.
     *
     * @return a schema rule that can handle a property declaration.
     */
    public Rule<JDefinedClass, JDefinedClass> getPropertyRule() {
        return new PropertyRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a "required"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "required" declaration.
     */
    public Rule<JDocCommentable, JDocCommentable> getRequiredRule() {
        return new RequiredRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a "required"
     * declaration is not found in the schema.
     *
     * @return a schema rule that can handle the "required" declaration.
     */
    public Rule<JDocCommentable, JDocCommentable> getNotRequiredRule() {
        return new NotRequiredRule(this);
    }

    /**
     * Provides a rule instance that should be applied to a node to find its
     * equivalent Java type. Typically invoked for properties, arrays, etc for
     * which a Java type must be found/generated.
     *
     * @return a schema rule that can find/generate the relevant Java type for a
     *         given schema node.
     */
    public Rule<JClassContainer, JType> getTypeRule() {
        return new TypeRule(this);
    }

    /**
     * Provides a rule instance that should be applied when an
     * "additionalProperties" declaration is found in the schema.
     *
     * @return a schema rule that can handle the "additionalProperties"
     *         declaration.
     */
    public Rule<JDefinedClass, JDefinedClass> getAdditionalPropertiesRule() {
        return new AdditionalPropertiesRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a "title"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "title" declaration.
     */
    public Rule<JDocCommentable, JDocComment> getTitleRule() {
        return new TitleRule();
    }

    /**
     * Provides a rule instance that should be applied when a schema declaration
     * is found in the schema.
     *
     * @return a schema rule that can handle a schema declaration.
     */
    public Rule<JClassContainer, JType> getSchemaRule() {
        return new SchemaRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a property
     * declaration is found in the schema to assign any appropriate default
     * value to that property.
     *
     * @return a schema rule that can handle the "default" declaration.
     */
    public Rule<JFieldVar, JFieldVar> getDefaultRule() {
        return new DefaultRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a property
     * declaration is found in the schema, to assign any minimum/maximum
     * validation on that property
     *
     * @return a schema rule that can handle the "default" declaration.
     */
    public Rule<JFieldVar, JFieldVar> getMinimumMaximumRule() {
        return new MinimumMaximumRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a property
     * declaration is found in the schema, to assign any size validation
     * (minItems/maxItems) on that property
     *
     * @return a schema rule that can handle the "default" declaration.
     */
    public Rule<JFieldVar, JFieldVar> getMinItemsMaxItemsRule() {
        return new MinItemsMaxItemsRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a property
     * declaration is found in the schema, to assign any size validation
     * (minLength/maxLength) on that property
     *
     * @return a schema rule that can handle the "default" declaration.
     */
    public Rule<JFieldVar, JFieldVar> getMinLengthMaxLengthRule() {
        return new MinLengthMaxLengthRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a property
     * declaration is found in the schema, to assign he digits validation
     * on that property.
     *
     * @return a schema rule that can handle the "digits" declaration.
     */
    public Rule<JFieldVar, JFieldVar> getDigitsRule() {
        return new DigitsRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a "pattern"
     * declaration is found in the schema for a property.
     *
     * @return a schema rule that can handle the "pattern" declaration.
     */
    public Rule<JFieldVar, JFieldVar> getPatternRule() {
        return new PatternRule(this);
    }

    /**
     * Provides a rule instance that should be applied when a property
     * declaration is found in the schema which itself contains properties, to
     * assign validation of the properties within that property
     *
     * @return a schema rule that can handle the "default" declaration.
     */
    public Rule<JFieldVar, JFieldVar> getValidRule() {
        return new ValidRule(this);
    }

    /**
     * Gets the configuration options that will influence the java code
     * generated by rules created by this factory.
     *
     * @return A configuration object containing all configuration property
     *         values.
     */
    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    /**
     * The generation config options for type generation. These config options
     * will influence the java code generated by rules created by this factory.
     *
     * @param generationConfig
     *            Generation config
     */
    public void setGenerationConfig(final GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
        this.nameHelper = new NameHelper(generationConfig);
    }

    /**
     * Gets the annotator that will in apply annotations to the generated code
     * to allow correct serialization and deserialization, according to the
     * chosen annotation style.
     *
     * @return an annotator that can annotate various code constructs for JSON
     *         support
     */
    public Annotator getAnnotator() {
        return annotator;
    }

    /**
     * The annotator used to mark up Java types with any annotations that are
     * required to build JSON compatible types
     *
     * @param annotator
     *            the annotator
     */
    public void setAnnotator(final Annotator annotator) {
        this.annotator = annotator;
    }

    /**
     * Provides a rule logger that abstracts the logging method of invoking frameworks
     *
     * @return a logger interface to native logging framework
     */
    public RuleLogger getLogger() {
        return logger;
    }

    /**
     * The logger the factory will provide to rules.
     *
     * @param logger
     *            the logger
     */
    public void setLogger(RuleLogger logger) {
        this.logger = logger;
    }

    /**
     * Gets the store that finds and saves JSON schemas
     *
     * @return a store that finds and caches schema objects during type
     *         generation.
     */
    public SchemaStore getSchemaStore() {
        return schemaStore;
    }

    /**
     * The object used by this factory to get and store schemas
     *
     * @param schemaStore
     *            schema store
     */
    public void setSchemaStore(final SchemaStore schemaStore) {
        this.schemaStore = schemaStore;
    }

    /**
     * Gets the name helper that is used to generate normalized Class and field
     * names.
     *
     * @return a name helper instance that can be used to normalize Class and
     *         field names.
     */
    public NameHelper getNameHelper() {
        return nameHelper;
    }

    public ReflectionHelper getReflectionHelper()    {
        return reflectionHelper;
    }


    /**
     * Provides a rule instance that should be applied when a "media"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "media" declaration.
     */
    public Rule<JType, JType> getMediaRule() {
        return new MediaRule();
    }

    /**
     * Provides a rule instance that adds methods for dynamically getting, setting, and
     * building properties.
     */
    public Rule<JDefinedClass, JDefinedClass> getDynamicPropertiesRule() {
        return new DynamicPropertiesRule(this);
    }

    public Rule<JDefinedClass, JDefinedClass> getBuilderRule(){
        return new BuilderRule(this, reflectionHelper);
    }

    public Rule<JDocCommentable, JDocComment> getJavaNameRule() {
        return new JavaNameRule();
    }

}
