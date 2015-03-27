<#include "internal/internal-page.ftl.html">
<#if pp.sourceDirectory == 'authenticated/' || pp.sourceDirectory == 'unknown/'>
<#include "${pp.sourceDirectory}/${pp.sourceDirectory?remove_ending('/')}_page.html">
</#if>