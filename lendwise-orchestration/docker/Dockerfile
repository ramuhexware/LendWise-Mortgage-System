# ==============================================================================
# Dockerfile for Oracle WebLogic & SOA Suite 12c (12.2.1.4)
# ==============================================================================

# Base image using Official Oracle SOA Suite 12c image
FROM container-registry.oracle.com/middleware/soasuite:12.2.1.4

LABEL maintainer="LendWise System Architecture Team"
LABEL description="Oracle WebLogic 12c & SOA Suite BPEL Runtime Environment"

# Set environment variables for WebLogic Domain
ENV ORACLE_HOME=/u01/oracle \
    DOMAIN_NAME=soa_domain \
    ADMIN_PORT=7001 \
    SOA_PORT=8001 \
    ADMIN_USERNAME=weblogic \
    ADMIN_PASSWORD=welcome1

USER oracle
WORKDIR /u01/oracle

# Copy BPEL composite source artifacts for auto-deployment
COPY soa-composites /u01/oracle/deploy/soa-composites

# Expose WebLogic Admin Console (7001) and SOA Server (8001)
EXPOSE 7001 8001

# Entrypoint script that starts WebLogic SOA domain and auto-deploys BPEL composites
CMD ["/u01/oracle/dockerd-entrypoint.sh"]
