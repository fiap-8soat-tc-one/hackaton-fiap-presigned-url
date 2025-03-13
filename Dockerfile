FROM public.ecr.aws/amazoncorretto/amazoncorretto:21 AS build
WORKDIR /app

ADD . .
RUN yum install -y maven && mvn compile dependency:copy-dependencies -DincludeScope=runtime

FROM public.ecr.aws/lambda/java:21

COPY --from=build /app/target/classes/  ${LAMBDA_TASK_ROOT}
COPY --from=build /app/target/dependency/  ${LAMBDA_TASK_ROOT}/lib/
CMD ["com.fiap.hackaton.functions.LambdaHandler::handleRequest"]
