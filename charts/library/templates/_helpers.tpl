{{/* Chart name. */}}
{{- define "library.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Full object name built from the release name, so the chart can be installed twice in one cluster.
Usage: include "library.fullname" .            -> <release>-library (or <release> if it already contains "library")
       include "library.componentName" (dict "root" . "component" "postgres") -> <fullname>-postgres
*/}}
{{- define "library.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else if contains (include "library.name" .) .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name (include "library.name" .) | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "library.componentName" -}}
{{- if eq .component "app" }}
{{- include "library.fullname" .root }}
{{- else }}
{{- printf "%s-%s" (include "library.fullname" .root) .component | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Selector labels for one component. Usage: include "library.selectorLabels" (dict "root" . "component" "app")
*/}}
{{- define "library.selectorLabels" -}}
app.kubernetes.io/name: {{ include "library.name" .root }}
app.kubernetes.io/instance: {{ .root.Release.Name }}
app.kubernetes.io/component: {{ .component }}
{{- end }}

{{/*
Full label set. Usage: include "library.labels" (dict "root" . "component" "app")
*/}}
{{- define "library.labels" -}}
helm.sh/chart: {{ printf "%s-%s" .root.Chart.Name .root.Chart.Version | replace "+" "_" }}
{{ include "library.selectorLabels" . }}
app.kubernetes.io/version: {{ .root.Values.image.tag | default .root.Chart.AppVersion | quote }}
app.kubernetes.io/part-of: library
app.kubernetes.io/managed-by: {{ .root.Release.Service }}
{{- end }}

{{- define "library.image" -}}
{{- printf "%s:%s" .Values.image.repository (.Values.image.tag | default .Chart.AppVersion) }}
{{- end }}

{{/*
Pod template labels: no chart/app version here, so a new app tag does not restart postgres or keycloak.
Usage: include "library.podLabels" (dict "root" . "component" "postgres")
*/}}
{{- define "library.podLabels" -}}
{{ include "library.selectorLabels" . }}
app.kubernetes.io/part-of: library
{{- if eq .component "app" }}
app.kubernetes.io/version: {{ .root.Values.image.tag | default .root.Chart.AppVersion | quote }}
{{- end }}
{{- end }}
