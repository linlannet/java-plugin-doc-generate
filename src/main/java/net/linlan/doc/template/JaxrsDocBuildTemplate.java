package net.linlan.doc.template;

import net.linlan.doc.builder.ProjectDocConfigBuilder;
import net.linlan.doc.common.util.CollectionUtil;
import net.linlan.doc.common.util.RandomUtil;
import net.linlan.doc.common.util.UrlUtil;
import net.linlan.doc.common.util.ValidateUtil;
import net.linlan.doc.constants.*;
import net.linlan.doc.handler.JaxrsHeaderHandler;
import net.linlan.doc.handler.JaxrsPathHandler;
import net.linlan.doc.helper.FormDataBuildHelper;
import net.linlan.doc.helper.JsonBuildHelper;
import net.linlan.doc.helper.ParamsBuildHelper;
import net.linlan.doc.model.*;
import net.linlan.doc.model.request.ApiRequestExample;
import net.linlan.doc.model.request.CurlRequest;
import net.linlan.doc.model.request.JaxrsPathMapping;
import com.thoughtworks.qdox.model.*;
import com.thoughtworks.qdox.model.expression.AnnotationValue;
import net.linlan.doc.utils.*;
import net.linlan.doc.utils.*;
import net.linlan.doc.constants.*;
import net.linlan.doc.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Build documents for JAX RS
 *
 * @author Zxq
 * @since 2021/7/15
 */
public class JaxrsDocBuildTemplate implements IDocBuildTemplate<ApiDoc> {

    private static Logger log = Logger.getLogger(SpringBootDocBuildTemplate.class.getName());
    /**
     * api index
     */
    private final AtomicInteger atomicInteger = new AtomicInteger(1);
    /**
     * headers
     */
    private List<ApiReqParam> headers;

    @Override
    public List<ApiDoc> getApiData(ProjectDocConfigBuilder projectBuilder) {
        ApiConfig apiConfig = projectBuilder.getApiConfig();
        this.headers = apiConfig.getRequestHeaders();
        List<ApiDoc> apiDocList = new ArrayList<>();
        int order = 0;
        Collection<JavaClass> classes = projectBuilder.getJavaProjectBuilder().getClasses();
        boolean setCustomOrder = false;
        // exclude  class is ignore
        for (JavaClass cls : classes) {
            if (StringUtils.isNotEmpty(apiConfig.getPackageFilters())) {
                // from smart config
                if (!DocUtil.isMatch(apiConfig.getPackageFilters(), cls.getCanonicalName())) {
                    continue;
                }
            }
            // from tag
            DocletTag ignoreTag = cls.getTagByName(DocTags.IGNORE);
            if (!checkController(cls) || Objects.nonNull(ignoreTag)) {
                continue;
            }
            String strOrder = JavaClassUtil.getClassTagsValue(cls, DocTags.ORDER, Boolean.TRUE);
            order++;
            if (ValidateUtil.isNonnegativeInteger(strOrder)) {
                setCustomOrder = true;
                order = Integer.parseInt(strOrder);
            }
            List<ApiMethodDoc> apiMethodDocs = buildControllerMethod(cls, apiConfig, projectBuilder);
            this.handleApiDoc(cls, apiDocList, apiMethodDocs, order, apiConfig.isMd5EncryptedHtmlName());
        }
        // sort
        if (apiConfig.isSortByTitle()) {
            Collections.sort(apiDocList);
        } else if (setCustomOrder) {
            // while set custom oder
            return apiDocList.stream()
                    .sorted(Comparator.comparing(ApiDoc::getOrder))
                    .peek(p -> p.setOrder(atomicInteger.getAndAdd(1))).collect(Collectors.toList());
        }
        return apiDocList;

    }

    @Override
    public ApiDoc getSingleApiData(ProjectDocConfigBuilder projectBuilder, String apiClassName) {
        return null;
    }

    @Override
    public boolean ignoreReturnObject(String typeName, List<String> ignoreParams) {
        return false;
    }

    /**
     * Analyze resource method
     *
     * @param cls            cls
     * @param apiConfig      apiConfig
     * @param projectBuilder projectBuilder
     * @return List<ApiMethodDoc>
     */
    private List<ApiMethodDoc> buildControllerMethod(final JavaClass cls, ApiConfig apiConfig,
                                                     ProjectDocConfigBuilder projectBuilder) {
        String clazName = cls.getCanonicalName();
        boolean paramsDataToTree = projectBuilder.getApiConfig().isParamsDataToTree();
        String group = JavaClassUtil.getClassTagsValue(cls, DocTags.GROUP, Boolean.TRUE);
        String classAuthor = JavaClassUtil.getClassTagsValue(cls, DocTags.AUTHOR, Boolean.TRUE);
        List<JavaAnnotation> classAnnotations = this.getAnnotations(cls);
        String baseUrl = "";
        for (JavaAnnotation annotation : classAnnotations) {
            String annotationName = annotation.getType().getValue();
            if (JAXRSAnnotations.JAX_PATH.equals(annotationName) ||
                    DocGlobalConstants.JAX_PATH_FULLY.equals(annotationName)) {
                baseUrl = StringUtilsExt.removeQuotes(DocUtil.getRequestHeaderValue(annotation));
            }
        }

        List<JavaMethod> methods = cls.getMethods();
        List<DocJavaMethod> docJavaMethods = new ArrayList<>(methods.size());
        // filter  private method
        for (JavaMethod method : methods) {
            if (method.isPrivate()) {
                continue;
            }
            docJavaMethods.add(DocJavaMethod.builder().setJavaMethod(method));
        }
        JavaClass parentClass = cls.getSuperJavaClass();
        if (Objects.nonNull(parentClass) && !"Object".equals(parentClass.getSimpleName())) {
            Map<String, JavaType> actualTypesMap = JavaClassUtil.getActualTypesMap(parentClass);
            List<JavaMethod> parentMethodList = parentClass.getMethods();
            for (JavaMethod method : parentMethodList) {
                docJavaMethods.add(DocJavaMethod.builder().setJavaMethod(method).setActualTypesMap(actualTypesMap));
            }
        }
        List<ApiMethodDoc> methodDocList = new ArrayList<>(methods.size());
        int methodOrder = 0;
        for (DocJavaMethod docJavaMethod : docJavaMethods) {
            JavaMethod method = docJavaMethod.getJavaMethod();
            if (checkCondition(method)) continue;
            // new api doc
            //handle request mapping
            JaxrsPathMapping jaxPathMapping = new JaxrsPathHandler()
                    .handle(projectBuilder, baseUrl, method);
            if (Objects.isNull(jaxPathMapping)) {
                continue;
            }
            if (StringUtils.isEmpty(method.getComment()) && apiConfig.isStrict()) {
                throw new RuntimeException("Unable to find comment for method " + method.getName() + " in " + cls.getCanonicalName());
            }
            ApiMethodDoc apiMethodDoc = new ApiMethodDoc();
            DocletTag downloadTag = method.getTagByName(DocTags.DOWNLOAD);
            if (Objects.nonNull(downloadTag)) {
                apiMethodDoc.setDownload(true);
            }
            DocletTag pageTag = method.getTagByName(DocTags.PAGE);
            if (Objects.nonNull(pageTag)) {
                String pageUrl = projectBuilder.getServerUrl() + "/" + pageTag.getValue();
                apiMethodDoc.setPage(UrlUtil.simplifyUrl(pageUrl));
            }
            DocletTag docletTag = method.getTagByName(DocTags.GROUP);
            apiMethodDoc.setGroup(group);
            if (Objects.nonNull(docletTag)) {
                apiMethodDoc.setGroup(docletTag.getValue());
            }
            methodOrder++;
            apiMethodDoc.setName(method.getName());
            apiMethodDoc.setOrder(methodOrder);
            String comment = DocUtil.getEscapeAndCleanComment(method.getComment());
            apiMethodDoc.setDesc(comment);
            String methodUid = DocUtil.generateId(clazName + method.getName());
            apiMethodDoc.setMethodId(methodUid);
            String apiNoteValue = DocUtil.getNormalTagComments(method, DocTags.API_NOTE, cls.getName());
            if (StringUtils.isEmpty(apiNoteValue)) {
                apiNoteValue = method.getComment();
            }
            Map<String, String> authorMap = DocUtil.getParamsComments(method, DocTags.AUTHOR, cls.getName());
            String authorValue = String.join(", ", new ArrayList<>(authorMap.keySet()));
            if (apiConfig.isShowAuthor() && StringUtils.isNotEmpty(authorValue)) {
                apiMethodDoc.setAuthor(JsonUtil.toPrettyFormat(authorValue));
            }
            if (apiConfig.isShowAuthor() && StringUtils.isEmpty(authorValue)) {
                apiMethodDoc.setAuthor(classAuthor);
            }
            apiMethodDoc.setDetail(apiNoteValue);
            List<ApiReqParam> ApiReqParams = new JaxrsHeaderHandler().handle(method, projectBuilder);
            apiMethodDoc.setType(jaxPathMapping.getMethodType());
            apiMethodDoc.setUrl(jaxPathMapping.getUrl());
            apiMethodDoc.setServerUrl(projectBuilder.getServerUrl());
            apiMethodDoc.setPath(jaxPathMapping.getShortUrl());
            apiMethodDoc.setDeprecated(jaxPathMapping.isDeprecated());
            List<JavaParameter> javaParameters = method.getParameters();

            setTornaArrayTags(javaParameters, apiMethodDoc, docJavaMethod.getJavaMethod().getReturns());
            // apiMethodDoc.setIsRequestArray();
            ApiMethodReqParam apiMethodReqParam = requestParams(docJavaMethod, projectBuilder);
            // build request params
            if (paramsDataToTree) {
                apiMethodDoc.setPathParams(ApiParamTreeUtil.apiParamToTree(apiMethodReqParam.getPathParams()));
                apiMethodDoc.setQueryParams(ApiParamTreeUtil.apiParamToTree(apiMethodReqParam.getQueryParams()));
                apiMethodDoc.setRequestParams(ApiParamTreeUtil.apiParamToTree(apiMethodReqParam.getRequestParams()));
            } else {
                apiMethodDoc.setPathParams(apiMethodReqParam.getPathParams());
                apiMethodDoc.setQueryParams(apiMethodReqParam.getQueryParams());
                apiMethodDoc.setRequestParams(apiMethodReqParam.getRequestParams());
            }

            List<ApiReqParam> allApiReqParams;
            allApiReqParams = ApiReqParams;
            if (this.headers != null) {
                allApiReqParams = Stream.of(this.headers, ApiReqParams)
                        .flatMap(Collection::stream).distinct().collect(Collectors.toList());
            }
            allApiReqParams.removeIf(ApiReqParam -> {
                if (StringUtils.isEmpty(ApiReqParam.getPathPatterns())
                        && StringUtils.isEmpty(ApiReqParam.getExcludePathPatterns())) {
                    return false;
                } else {
                    boolean flag = DocPathUtil.matches(jaxPathMapping.getShortUrl(), ApiReqParam.getPathPatterns()
                            , ApiReqParam.getExcludePathPatterns());
                    return !flag;
                }
            });
            //reduce create in template
            apiMethodDoc.setHeaders(this.createDocRenderHeaders(allApiReqParams, apiConfig.isAdoc()));
            apiMethodDoc.setRequestHeaders(allApiReqParams);

            // build request json
            ApiRequestExample requestExample = buildReqJson(docJavaMethod, apiMethodDoc, jaxPathMapping.getMethodType(),
                    projectBuilder);
            String requestJson = requestExample.getExampleBody();
            // set request example detail
            apiMethodDoc.setRequestExample(requestExample);
            apiMethodDoc.setRequestUsage(requestJson == null ? requestExample.getUrl() : requestJson);
            // build response usage
            String responseValue = DocUtil.getNormalTagComments(method, DocTags.API_RESPONSE, cls.getName());
            if (StringUtils.isNotEmpty(responseValue)) {
                apiMethodDoc.setResponseUsage(responseValue);
            } else {
                apiMethodDoc.setResponseUsage(JsonBuildHelper.buildReturnJson(docJavaMethod, projectBuilder));
            }
            // auto mark file download
            if (Objects.isNull(docletTag)) {
                apiMethodDoc.setDownload(docJavaMethod.isDownload());
            }
            // build response params
            List<ApiParam> responseParams = buildReturnApiParams(docJavaMethod, projectBuilder);
            if (paramsDataToTree) {
                responseParams = ApiParamTreeUtil.apiParamToTree(responseParams);
            }
            apiMethodDoc.setReturnSchema(docJavaMethod.getReturnSchema());
            apiMethodDoc.setRequestSchema(docJavaMethod.getRequestSchema());
            apiMethodDoc.setResponseParams(responseParams);
            methodDocList.add(apiMethodDoc);
        }
        return methodDocList;
    }

    /**
     * @param method method
     * @return boolean
     */
    private boolean checkCondition(JavaMethod method) {
        return method.isPrivate() || Objects.nonNull(method.getTagByName(DocTags.IGNORE));
    }


    /**
     * getAnnotations
     *
     * @param cls java-class
     * @return All javaAnnotation
     */
    private List<JavaAnnotation> getAnnotations(JavaClass cls) {
        List<JavaAnnotation> annotationsList = new ArrayList<>(cls.getAnnotations());
        boolean flag = annotationsList.stream().anyMatch(item -> {
            String annotationName = item.getType().getValue();
            return JAXRSAnnotations.JAX_PATH.equals(annotationName) ||
                    DocGlobalConstants.JAX_PATH_FULLY.equals(annotationName);
        });
        // child override parent set
        if (flag) {
            return annotationsList;
        }
        JavaClass superJavaClass = cls.getSuperJavaClass();
        if (Objects.nonNull(superJavaClass) && !"Object".equals(superJavaClass.getSimpleName())) {
            annotationsList.addAll(getAnnotations(superJavaClass));
        }
        return annotationsList;
    }

    /**
     * build request params
     *
     * @param docJavaMethod docJavaMethod
     * @param builder       builder
     * @return ApiMethodReqParam
     */
    private ApiMethodReqParam requestParams(final DocJavaMethod docJavaMethod, ProjectDocConfigBuilder builder) {
        JavaMethod javaMethod = docJavaMethod.getJavaMethod();
        boolean isStrict = builder.getApiConfig().isStrict();
        String className = javaMethod.getDeclaringClass().getCanonicalName();
        Map<String, String> replacementMap = builder.getReplaceClassMap();
        Map<String, String> paramTagMap = DocUtil.getParamsComments(javaMethod, DocTags.PARAM, className);
        Map<String, String> paramsComments = DocUtil.getParamsComments(javaMethod, DocTags.PARAM, null);
        List<ApiParam> paramList = new ArrayList<>();
        List<JavaParameter> parameterList = javaMethod.getParameters();
        if (parameterList.size() < 1) {
            return ApiMethodReqParam.builder()
                    .setPathParams(new ArrayList<>(0))
                    .setQueryParams(paramList)
                    .setRequestParams(new ArrayList<>(0));
        }
        Map<String, String> constantsMap = builder.getConstantsMap();
        boolean requestFieldToUnderline = builder.getApiConfig().isRequestFieldToUnderline();
        Set<String> ignoreSets = ignoreParamsSets(javaMethod);
        Map<String, JavaType> actualTypesMap = docJavaMethod.getActualTypesMap();
        out:
        for (JavaParameter parameter : parameterList) {
            String paramName = parameter.getName();
            if (ignoreSets.contains(paramName)) {
                continue;
            }
            JavaType javaType = parameter.getType();
            if (Objects.nonNull(actualTypesMap) && Objects.nonNull(actualTypesMap.get(javaType.getCanonicalName()))) {
                javaType = actualTypesMap.get(javaType.getCanonicalName());
            }
            String typeName = javaType.getGenericCanonicalName();
            String simpleName = javaType.getValue().toLowerCase();
            String fullTypeName = javaType.getFullyQualifiedName();
            String simpleTypeName = javaType.getValue();
            String commentClass = paramTagMap.get(paramName);
            String rewriteClassName = getRewriteClassName(replacementMap, fullTypeName, commentClass);
            // rewrite class
            if (DocUtil.isClassName(rewriteClassName)) {
                typeName = rewriteClassName;
                fullTypeName = DocClassUtil.getSimpleName(rewriteClassName);
            }
            if (JavaClassValidateUtil.isMvcIgnoreParams(typeName, builder.getApiConfig().getIgnoreRequestParams())) {
                continue;
            }
            fullTypeName = DocClassUtil.rewriteRequestParam(fullTypeName);
            typeName = DocClassUtil.rewriteRequestParam(typeName);
            if (!paramTagMap.containsKey(paramName) && JavaClassValidateUtil.isPrimitive(fullTypeName) && isStrict) {
                throw new RuntimeException("ERROR: Unable to find javadoc @QueryParam for actual param \""
                        + paramName + "\" in method " + javaMethod.getName() + " from " + className);
            }
            String comment = this.paramCommentResolve(paramTagMap.get(paramName));
            if (requestFieldToUnderline) {
                paramName = StringUtilsExt.camelToUnderline(paramName);
            }
            //file upload
            if (JavaClassValidateUtil.isFile(typeName)) {
                ApiParam param = ApiParam.of().setField(paramName).setType("file")
                        .setId(paramList.size() + 1).setQueryParam(true)
                        .setRequired(true).setVersion(DocGlobalConstants.DEFAULT_VERSION)
                        .setDesc(comment);
                if (typeName.contains("[]") || typeName.endsWith(">")) {
                    comment = comment + "(array of file)";
                    param.setDesc(comment);
                    param.setHasItems(true);
                }
                paramList.add(param);
                continue;
            }
            String mockValue = createMockValue(paramsComments, paramName, typeName, simpleTypeName);
            JavaClass javaClass = builder.getJavaProjectBuilder().getClassByName(fullTypeName);
            List<JavaAnnotation> annotations = parameter.getAnnotations();
            List<String> groupClasses = JavaClassUtil.getParamGroupJavaClass(annotations);
            String strRequired = "false";
            boolean isPathVariable = false;
            boolean isRequestBody = false;
            for (JavaAnnotation annotation : annotations) {
                String annotationName = annotation.getType().getValue();
                if (JAXRSAnnotations.JAX_HEADER_PARAM.equals(annotationName)) {
                    continue out;
                }
                // path param
                if (JAXRSAnnotations.JAX_PATH_PARAM.equals(annotationName) ) {
                    isPathVariable = true;
                    paramName = getParamName(paramName, annotation);
                    for (Map.Entry<String, String> entry : constantsMap.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if (paramName.contains(key)) {
                            paramName = paramName.replace(key, value);
                        }
                        // replace mockValue
                        if (mockValue.contains(key)) {
                            mockValue = mockValue.replace(key, value);
                        }
                    }
                }
            }
            boolean required = false;
            boolean queryParam = !isPathVariable;
            if (JavaClassValidateUtil.isCollection(fullTypeName) || JavaClassValidateUtil.isArray(fullTypeName)) {
                if (JavaClassValidateUtil.isCollection(typeName)) {
                    typeName = typeName + "<T>";
                }
                String[] gicNameArr = DocClassUtil.getSimpleGicName(typeName);
                String gicName = gicNameArr[0];
                if (JavaClassValidateUtil.isArray(gicName)) {
                    gicName = gicName.substring(0, gicName.indexOf("["));
                }
                JavaClass gicJavaClass = builder.getJavaProjectBuilder().getClassByName(gicName);
                if (gicJavaClass.isEnum()) {
                    Object value = JavaClassUtil.getEnumValue(gicJavaClass, Boolean.TRUE);
                    ApiParam param = ApiParam.of().setField(paramName).setDesc(comment + ",[array of enum]")
                            .setRequired(required)
                            .setPathParam(isPathVariable)
                            .setQueryParam(queryParam)
                            .setId(paramList.size() + 1)
                            .setType("array").setValue(String.valueOf(value));
                    paramList.add(param);
                } else if (JavaClassValidateUtil.isPrimitive(gicName)) {
                    String shortSimple = DocClassUtil.processTypeNameForParams(gicName);
                    ApiParam param = ApiParam.of()
                            .setField(paramName)
                            .setDesc(comment + ",[array of " + shortSimple + "]")
                            .setRequired(required)
                            .setPathParam(isPathVariable)
                            .setQueryParam(queryParam)
                            .setId(paramList.size() + 1)
                            .setType("array")
                            .setValue(DocUtil.getValByTypeAndFieldName(gicName, paramName));
                    paramList.add(param);
                } else {
                    throw new RuntimeException("Dubbo Rest can't support binding Collection on method "
                            + javaMethod.getName() + ",Check it in " + javaMethod.getDeclaringClass()
                            .getCanonicalName());
                }
            } else if (JavaClassValidateUtil.isPrimitive(fullTypeName)) {
                ApiParam param = ApiParam.of()
                        .setField(paramName)
                        .setType(DocClassUtil.processTypeNameForParams(simpleName))
                        .setId(paramList.size() + 1)
                        .setPathParam(isPathVariable)
                        .setQueryParam(queryParam)
                        .setValue(mockValue)
                        .setDesc(comment)
                        .setRequired(required)
                        .setVersion(DocGlobalConstants.DEFAULT_VERSION);
                paramList.add(param);
            } else if (JavaClassValidateUtil.isMap(fullTypeName)) {
                log.warning("When using doc-generate, it is not recommended to use Map to receive parameters, Check it in "
                        + javaMethod.getDeclaringClass().getCanonicalName() + "#" + javaMethod.getName());
                if (JavaClassValidateUtil.isMap(typeName)) {
                    ApiParam apiParam = ApiParam.of()
                            .setField(paramName)
                            .setType("map")
                            .setId(paramList.size() + 1)
                            .setPathParam(isPathVariable)
                            .setQueryParam(queryParam)
                            .setDesc(comment)
                            .setRequired(required)
                            .setVersion(DocGlobalConstants.DEFAULT_VERSION);
                    paramList.add(apiParam);
                    continue;
                }
                String[] gicNameArr = DocClassUtil.getSimpleGicName(typeName);
                if (JavaClassValidateUtil.isPrimitive(gicNameArr[1])) {
                    ApiParam apiParam = ApiParam.of()
                            .setField(paramName)
                            .setType("map")
                            .setId(paramList.size() + 1)
                            .setPathParam(isPathVariable)
                            .setQueryParam(queryParam)
                            .setDesc(comment)
                            .setRequired(required)
                            .setVersion(DocGlobalConstants.DEFAULT_VERSION);
                    paramList.add(apiParam);
                } else {
                    paramList.addAll(ParamsBuildHelper.buildParams(gicNameArr[1], DocGlobalConstants.EMPTY, 0,
                            "true", Boolean.FALSE, new HashMap<>(),
                            builder, groupClasses, 0, Boolean.FALSE));
                }

            }
            // param is enum
            else if (javaClass.isEnum()) {
                String o = JavaClassUtil.getEnumParams(javaClass);
                Object value = JavaClassUtil.getEnumValue(javaClass, true);
                ApiParam param = ApiParam.of().setField(paramName)
                        .setId(paramList.size() + 1)
                        .setPathParam(isPathVariable)
                        .setQueryParam(queryParam)
                        .setValue(String.valueOf(value))
                        .setType("enum").setDesc(StringUtilsExt.removeQuotes(o))
                        .setRequired(required)
                        .setVersion(DocGlobalConstants.DEFAULT_VERSION)
                        .setEnumValues(JavaClassUtil.getEnumValues(javaClass));
                paramList.add(param);
            } else {
                paramList.addAll(ParamsBuildHelper.buildParams(typeName, DocGlobalConstants.EMPTY, 0,
                        "true", Boolean.FALSE, new HashMap<>(), builder, groupClasses, 0, Boolean.FALSE));
            }
        }
        List<ApiParam> pathParams = new ArrayList<>();
        List<ApiParam> queryParams = new ArrayList<>();
        List<ApiParam> bodyParams = new ArrayList<>();
        for (ApiParam param : paramList) {
            param.setValue(StringUtilsExt.removeDoubleQuotes(param.getValue()));
            if (param.isPathParam()) {
                param.setId(pathParams.size() + 1);
                pathParams.add(param);
            } else {
                param.setId(queryParams.size() + 1);
                queryParams.add(param);
            }
        }
        return ApiMethodReqParam.builder()
                .setRequestParams(bodyParams)
                .setPathParams(pathParams)
                .setQueryParams(queryParams);
    }

    private String getParamName(String paramName, JavaAnnotation annotation) {
        AnnotationValue annotationValue = annotation.getProperty(DocAnnotationConstants.VALUE_PROP);
        if (Objects.nonNull(annotationValue)) {
            paramName = StringUtilsExt.removeQuotes(annotationValue.toString());
        }
        AnnotationValue annotationOfName = annotation.getProperty(DocAnnotationConstants.NAME_PROP);
        if (Objects.nonNull(annotationOfName)) {
            paramName = StringUtilsExt.removeQuotes(annotationOfName.toString());
        }
        return paramName;
    }

    private String createMockValue(Map<String, String> paramsComments, String paramName, String typeName, String simpleTypeName) {
        String mockValue = "";
        if (JavaClassValidateUtil.isPrimitive(typeName)) {
            mockValue = paramsComments.get(paramName);
            if (Objects.nonNull(mockValue) && mockValue.contains("|")) {
                mockValue = mockValue.substring(mockValue.lastIndexOf("|") + 1);
            } else {
                mockValue = "";
            }
            if (StringUtils.isEmpty(mockValue)) {
                mockValue = DocUtil.getValByTypeAndFieldName(simpleTypeName, paramName, Boolean.TRUE);
            }
        }
        return mockValue;
    }

    private static void setTornaArrayTags(List<JavaParameter> javaParameters, ApiMethodDoc apiMethodDoc, JavaClass returnClass) {

        apiMethodDoc.setIsResponseArray(0);
        apiMethodDoc.setIsRequestArray(0);
        //response tags
        if (JavaClassValidateUtil.isCollection(returnClass.getFullyQualifiedName()) ||
                JavaClassValidateUtil.isArray(returnClass.getFullyQualifiedName())) {
            apiMethodDoc.setIsResponseArray(1);
            String gicType;
            String simpleGicType;
            String typeName = returnClass.getGenericFullyQualifiedName();
            gicType = getType(typeName);
            simpleGicType = gicType.substring(gicType.lastIndexOf(".") + 1).toLowerCase();
            apiMethodDoc.setResponseArrayType(JavaClassValidateUtil.isPrimitive(gicType) ? simpleGicType : DocGlobalConstants.OBJECT);
        }
        //request tags
        if (CollectionUtil.isNotEmpty(javaParameters)) {
            for (JavaParameter parameter : javaParameters) {
                String gicType;
                String simpleGicType;
                String typeName = parameter.getType().getGenericFullyQualifiedName();
                String name = parameter.getType().getFullyQualifiedName();
                gicType = getType(typeName);
                simpleGicType = gicType.substring(gicType.lastIndexOf(".") + 1).toLowerCase();
                // is array
                if (JavaClassValidateUtil.isCollection(name) || JavaClassValidateUtil.isArray(name)) {
                    boolean hasRequestBody = false;
                    //param has @RequestBody ?
                    List<JavaAnnotation> annotations = parameter.getAnnotations();
                    for (JavaAnnotation annotation : annotations) {
                        if (DocGlobalConstants.REQUEST_BODY_FULLY.equals(annotation.getType().getName())) {
                            hasRequestBody = true;
                            break;
                        }
                    }
                    //formData - multiple data
                    if (!hasRequestBody && javaParameters.size() > 1) {
                        return;
                    } else {
                        apiMethodDoc.setIsRequestArray(1);
                        if (JavaClassValidateUtil.isPrimitive(gicType)) {
                            apiMethodDoc.setRequestArrayType(simpleGicType);
                        } else {
                            apiMethodDoc.setRequestArrayType(DocGlobalConstants.OBJECT);
                        }
                    }
                }
            }
        }
    }

    private ApiRequestExample buildReqJson(DocJavaMethod javaMethod, ApiMethodDoc apiMethodDoc, String methodType,
                                           ProjectDocConfigBuilder configBuilder) {
        JavaMethod method = javaMethod.getJavaMethod();
        Map<String, String> pathParamsMap = new LinkedHashMap<>();
        List<JavaAnnotation> methodAnnotations = method.getAnnotations();
        List<JavaParameter> parameterList = method.getParameters();
        List<ApiReqParam> reqHeaderList = apiMethodDoc.getRequestHeaders();
        if (parameterList.size() < 1) {
            CurlRequest curlRequest = CurlRequest.builder()
                    .setContentType(apiMethodDoc.getContentType())
                    .setType(methodType)
                    .setReqHeaders(reqHeaderList)
                    .setUrl(apiMethodDoc.getUrl());
            String format = CurlUtil.toCurl(curlRequest);
            return ApiRequestExample.builder().setUrl(apiMethodDoc.getUrl()).setExampleBody(format);
        }
        Set<String> ignoreSets = ignoreParamsSets(method);
        Map<String, JavaType> actualTypesMap = javaMethod.getActualTypesMap();
        Map<String, String> constantsMap = configBuilder.getConstantsMap();
        boolean requestFieldToUnderline = configBuilder.getApiConfig().isRequestFieldToUnderline();
        Map<String, String> replacementMap = configBuilder.getReplaceClassMap();
        Map<String, String> paramsComments = DocUtil.getParamsComments(method, DocTags.PARAM, null);
        List<FormData> formDataList = new ArrayList<>();
        ApiRequestExample requestExample = ApiRequestExample.builder();
        for (JavaParameter parameter : parameterList) {
            JavaType javaType = parameter.getType();
            if (Objects.nonNull(actualTypesMap) && Objects.nonNull(actualTypesMap.get(javaType.getCanonicalName()))) {
                javaType = actualTypesMap.get(javaType.getCanonicalName());
            }
            String paramName = parameter.getName();
            if (ignoreSets.contains(paramName)) {
                continue;
            }
            String typeName = javaType.getFullyQualifiedName();
            String gicTypeName = javaType.getGenericCanonicalName();

            String commentClass = paramsComments.get(paramName);
            //ignore request params
            if (Objects.nonNull(commentClass) && commentClass.contains(DocTags.IGNORE)) {
                continue;
            }
            String rewriteClassName = this.getRewriteClassName(replacementMap, typeName, commentClass);
            // rewrite class
            if (DocUtil.isClassName(rewriteClassName)) {
                gicTypeName = rewriteClassName;
                typeName = DocClassUtil.getSimpleName(rewriteClassName);
            }
            if (JavaClassValidateUtil.isMvcIgnoreParams(typeName, configBuilder.getApiConfig()
                    .getIgnoreRequestParams())) {
                continue;
            }
            String simpleTypeName = javaType.getValue();
            typeName = DocClassUtil.rewriteRequestParam(typeName);
            gicTypeName = DocClassUtil.rewriteRequestParam(gicTypeName);
            //if params is collection
            if (JavaClassValidateUtil.isCollection(typeName)) {
                apiMethodDoc.setListParam(true);
            }
            JavaClass javaClass = configBuilder.getJavaProjectBuilder().getClassByName(typeName);
            String[] globGicName = DocClassUtil.getSimpleGicName(gicTypeName);
            String comment = this.paramCommentResolve(paramsComments.get(paramName));
            String mockValue = createMockValue(paramsComments, paramName, typeName, simpleTypeName);
            if (requestFieldToUnderline) {
                paramName = StringUtilsExt.camelToUnderline(paramName);
            }
            List<JavaAnnotation> annotations = parameter.getAnnotations();
            List<String> groupClasses = JavaClassUtil.getParamGroupJavaClass(annotations);
            boolean paramAdded = false;
            for (JavaAnnotation annotation : methodAnnotations) {
                String annotationName = annotation.getType().getValue();
                if (JAXRSAnnotations.JAX_CONSUMES.equals(annotationName) || paramAdded) {
                    AnnotationValue avalue = annotation.getProperty(DocAnnotationConstants.VALUE_PROP);
                    if (avalue != null && avalue.toString().contains("json") || avalue.toString().contains("JSON")) {
                        paramName = getParamName(paramName, annotation);
                        for (Map.Entry<String, String> entry : constantsMap.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            // replace param
                            if (paramName.contains(key)) {
                                paramName = paramName.replace(key, value);
                            }
                            // replace mockValue
                            if (mockValue.contains(key)) {
                                mockValue = mockValue.replace(key, value);
                            }
                        }

                        apiMethodDoc.setContentType(DocGlobalConstants.JSON_CONTENT_TYPE);
                        if (Objects.nonNull(configBuilder.getApiConfig().getRequestBodyAdvice())
                                && Objects.isNull(method.getTagByName(DocTags.IGNORE_REQUEST_BODY_ADVICE))) {
                            String requestBodyAdvice = configBuilder.getApiConfig()
                                    .getRequestBodyAdvice()
                                    .getClassName();
                            typeName = configBuilder.getApiConfig().getRequestBodyAdvice().getClassName();
                            gicTypeName = requestBodyAdvice + "<" + gicTypeName + ">";
                        }

                        if (JavaClassValidateUtil.isPrimitive(simpleTypeName)) {
                            StringBuilder builder = new StringBuilder();
                            builder.append("{\"")
                                    .append(paramName)
                                    .append("\":")
                                    .append(DocUtil.handleJsonStr(mockValue))
                                    .append("}");
                            requestExample.setJsonBody(JsonUtil.toPrettyFormat(builder.toString())).setJson(true);
                        } else {
                            String json = JsonBuildHelper.buildJson(typeName, gicTypeName, Boolean.FALSE, 0, new HashMap<>(),groupClasses, configBuilder);
                            requestExample.setJsonBody(JsonUtil.toPrettyFormat(json)).setJson(true);
                        }
                        paramAdded = true;
                    }
                }

                if (JAXRSAnnotations.JAX_PATH_PARAM.equals(annotationName)) {
                    if (javaClass.isEnum()) {
                        Object value = JavaClassUtil.getEnumValue(javaClass, Boolean.TRUE);
                        mockValue = StringUtilsExt.removeQuotes(String.valueOf(value));
                    }
                    pathParamsMap.put(paramName, mockValue);
                    paramAdded = true;
                }
                if (paramAdded) {
                    continue;
                }
                //file upload
                if (JavaClassValidateUtil.isFile(gicTypeName)) {
                    apiMethodDoc.setContentType(DocGlobalConstants.FILE_CONTENT_TYPE);
                    FormData formData = new FormData();
                    formData.setKey(paramName);
                    formData.setType("file");
                    formData.setDescription(comment);
                    formData.setValue(mockValue);
                    formDataList.add(formData);
                } else if (JavaClassValidateUtil.isPrimitive(typeName)) {
                    FormData formData = new FormData();
                    formData.setKey(paramName);
                    formData.setDescription(comment);
                    formData.setType("text");
                    formData.setValue(mockValue);
                    formDataList.add(formData);
                } else if (JavaClassValidateUtil.isArray(typeName) || JavaClassValidateUtil.isCollection(typeName)) {
                    String gicName = globGicName[0];
                    if (JavaClassValidateUtil.isArray(gicName)) {
                        gicName = gicName.substring(0, gicName.indexOf("["));
                    }
                    if (!JavaClassValidateUtil.isPrimitive(gicName)
                            && !configBuilder.getJavaProjectBuilder().getClassByName(gicName).isEnum()) {
                        throw new RuntimeException("Dubbo rest can't support binding Collection on method "
                                + method.getName() + "Check it in " + method.getDeclaringClass().getCanonicalName());
                    }
                    FormData formData = new FormData();
                    formData.setKey(paramName);
                    if (!paramName.contains("[]")) {
                        formData.setKey(paramName + "[]");
                    }
                    formData.setDescription(comment);
                    formData.setType("text");
                    formData.setValue(RandomUtil.randomValueByType(gicName));
                    formDataList.add(formData);
                } else if (javaClass.isEnum()) {
                    // do nothing
                    Object value = JavaClassUtil.getEnumValue(javaClass, Boolean.TRUE);
                    String strVal = StringUtilsExt.removeQuotes(String.valueOf(value));
                    FormData formData = new FormData();
                    formData.setKey(paramName);
                    formData.setType("text");
                    formData.setDescription(comment);
                    formData.setValue(strVal);
                    formDataList.add(formData);
                } else {
                    formDataList.addAll(FormDataBuildHelper.getFormData(gicTypeName, new HashMap<>(), 0, configBuilder, DocGlobalConstants.EMPTY));
                }
            }
        }
        requestExample.setFormDataList(formDataList);
        String[] paths = apiMethodDoc.getPath().split(";");
        String path = paths[0];
        String body;
        String exampleBody;
        String url;
        if (Methods.POST.getValue()
                .equals(methodType) || Methods.PUT.getValue()
                .equals(methodType)) {
            //for post put
            path = DocUtil.formatAndRemove(path, pathParamsMap);
            body = UrlUtil.urlJoin(DocGlobalConstants.EMPTY, DocUtil.formDataToMap(formDataList))
                    .replace("?", DocGlobalConstants.EMPTY);
            body = StringUtilsExt.removeQuotes(body);
            url = apiMethodDoc.getServerUrl() + "/" + path;
            url = UrlUtil.simplifyUrl(url);

            if (requestExample.isJson()) {
                if (StringUtils.isNotEmpty(body)) {
                    url = url + "?" + body;
                }
                CurlRequest curlRequest = CurlRequest.builder()
                        .setBody(requestExample.getJsonBody())
                        .setContentType(apiMethodDoc.getContentType())
                        .setType(methodType)
                        .setReqHeaders(reqHeaderList)
                        .setUrl(url);
                exampleBody = CurlUtil.toCurl(curlRequest);
            } else {
                CurlRequest curlRequest;
                if (StringUtils.isNotEmpty(body)) {
                    curlRequest = CurlRequest.builder()
                            .setBody(body)
                            .setContentType(apiMethodDoc.getContentType())
                            .setType(methodType)
                            .setReqHeaders(reqHeaderList)
                            .setUrl(url);
                } else {
                    curlRequest = CurlRequest.builder()
                            .setBody(requestExample.getJsonBody())
                            .setContentType(apiMethodDoc.getContentType())
                            .setType(methodType)
                            .setReqHeaders(reqHeaderList)
                            .setUrl(url);
                }
                exampleBody = CurlUtil.toCurl(curlRequest);
            }
            requestExample.setExampleBody(exampleBody).setUrl(url);
        } else {
            // for get delete
            pathParamsMap.putAll(DocUtil.formDataToMap(formDataList));
            path = DocUtil.formatAndRemove(path, pathParamsMap);
            url = UrlUtil.urlJoin(path, pathParamsMap);
            url = StringUtilsExt.removeQuotes(url);
            url = apiMethodDoc.getServerUrl() + "/" + url;
            url = UrlUtil.simplifyUrl(url);
            CurlRequest curlRequest = CurlRequest.builder()
                    .setBody(requestExample.getJsonBody())
                    .setContentType(apiMethodDoc.getContentType())
                    .setType(methodType)
                    .setReqHeaders(reqHeaderList)
                    .setUrl(url);
            exampleBody = CurlUtil.toCurl(curlRequest);
            requestExample.setExampleBody(exampleBody)
                    .setJsonBody(DocGlobalConstants.EMPTY)
                    .setUrl(url);
        }
        return requestExample;
    }

    private String getRewriteClassName(Map<String, String> replacementMap, String fullTypeName, String commentClass) {
        String rewriteClassName;
        if (Objects.nonNull(commentClass) && !DocGlobalConstants.NO_COMMENTS_FOUND.equals(commentClass)) {
            String[] comments = commentClass.split("\\|");
            rewriteClassName = comments[comments.length - 1];
            if (DocUtil.isClassName(rewriteClassName)) {
                return rewriteClassName;
            }
        }
        return replacementMap.get(fullTypeName);
    }

    private static String getType(String typeName) {
        String gicType;
        //get generic type
        if (typeName.contains("<")) {
            gicType = typeName.substring(typeName.indexOf("<") + 1, typeName.lastIndexOf(">"));
        } else {
            gicType = typeName;
        }
        if (gicType.contains("[")) {
            gicType = gicType.substring(0, gicType.indexOf("["));
        }
        return gicType;
    }

    private boolean checkController(JavaClass cls) {
        if (cls.isAnnotation() || cls.isEnum()) {
            return false;
        }
        JavaClass superClass = cls.getSuperJavaClass();
        List<JavaAnnotation> classAnnotations = new ArrayList<>();
        if (Objects.nonNull(superClass)) {
            classAnnotations.addAll(superClass.getAnnotations());
        }
        classAnnotations.addAll(cls.getAnnotations());
        for (JavaAnnotation annotation : classAnnotations) {
            String name = annotation.getType().getValue();
            if (JAXRSAnnotations.JAX_PATH.equals(name)) {
                return true;
            }
        }
        // use custom doc tag to support Feign.
        List<DocletTag> docletTags = cls.getTags();
        for (DocletTag docletTag : docletTags) {
            String value = docletTag.getName();
            if (DocTags.DUBBO_REST.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
