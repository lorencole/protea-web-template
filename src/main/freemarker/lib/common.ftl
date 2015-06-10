<#include "internal-page.ftl.html">
<#if pp.sourceDirectory == 'authenticated/' || pp.sourceDirectory == 'unknown/'>
<#include "../theme/${pp.sourceDirectory}${pp.sourceDirectory?remove_ending('/')}-lib.html" ignore_missing=true>
<#include "../theme/${pp.sourceDirectory}${pp.sourceDirectory?remove_ending('/')}-page.html">
</#if>