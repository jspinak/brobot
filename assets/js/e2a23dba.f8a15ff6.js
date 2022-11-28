"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[1346],{3905:(e,t,r)=>{r.d(t,{Zo:()=>u,kt:()=>p});var n=r(7294);function a(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function o(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function s(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?o(Object(r),!0).forEach((function(t){a(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):o(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function i(e,t){if(null==e)return{};var r,n,a=function(e,t){if(null==e)return{};var r,n,a={},o=Object.keys(e);for(n=0;n<o.length;n++)r=o[n],t.indexOf(r)>=0||(a[r]=e[r]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(n=0;n<o.length;n++)r=o[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(a[r]=e[r])}return a}var l=n.createContext({}),c=function(e){var t=n.useContext(l),r=t;return e&&(r="function"==typeof e?e(t):s(s({},t),e)),r},u=function(e){var t=c(e.components);return n.createElement(l.Provider,{value:t},e.children)},f={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},d=n.forwardRef((function(e,t){var r=e.components,a=e.mdxType,o=e.originalType,l=e.parentName,u=i(e,["components","mdxType","originalType","parentName"]),d=c(r),p=a,b=d["".concat(l,".").concat(p)]||d[p]||f[p]||o;return r?n.createElement(b,s(s({ref:t},u),{},{components:r})):n.createElement(b,s({ref:t},u))}));function p(e,t){var r=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=r.length,s=new Array(o);s[0]=d;var i={};for(var l in t)hasOwnProperty.call(t,l)&&(i[l]=t[l]);i.originalType=e,i.mdxType="string"==typeof e?e:a,s[1]=i;for(var c=2;c<o;c++)s[c]=r[c];return n.createElement.apply(null,s)}return n.createElement.apply(null,r)}d.displayName="MDXCreateElement"},2903:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>l,contentTitle:()=>s,default:()=>f,frontMatter:()=>o,metadata:()=>i,toc:()=>c});var n=r(7462),a=(r(7294),r(3905));const o={sidebar_position:1},s="Labeling Images",i={unversionedId:"labeling/labeling-intro",id:"labeling/labeling-intro",title:"Labeling Images",description:"Machine Vision research relies on supervised learning to train models, and supervised learning requires the",source:"@site/docs/labeling/labeling-intro.md",sourceDirName:"labeling",slug:"/labeling/labeling-intro",permalink:"/brobot/docs/labeling/labeling-intro",draft:!1,editUrl:"https://jspinak.github.io/brobot/docs/labeling/labeling-intro.md",tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"tutorialSidebar",previous:{title:"Building the Visual API",permalink:"/brobot/docs/tutorial-state-structure-builder/videos"},next:{title:"The CLASSIFY Action",permalink:"/brobot/docs/labeling/classify"}},l={},c=[],u={toc:c};function f(e){let{components:t,...o}=e;return(0,a.kt)("wrapper",(0,n.Z)({},u,o,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"labeling-images"},"Labeling Images"),(0,a.kt)("p",null,"Machine Vision research relies on supervised learning to train models, and supervised learning requires the\nuse of huge datasets of labeled images. "),(0,a.kt)("p",null,"These datasets are built by real people who put bounding boxes around cats (and other objects) on millions of images.",(0,a.kt)("br",{parentName:"p"}),"\n",(0,a.kt)("img",{alt:"people",src:r(9903).Z,width:"159",height:"46"})),(0,a.kt)("p",null,"The big guys can afford this, but it's too expensive and time-consuming for the rest of us.\nLuckily, they've made many of these datasets available, but what if we want to classify something other than cats?",(0,a.kt)("br",{parentName:"p"}),"\n",(0,a.kt)("img",{alt:"google",src:r(882).Z,width:"102",height:"83"}),"\n",(0,a.kt)("img",{alt:"stanford",src:r(4577).Z,width:"58",height:"97"})),(0,a.kt)("p",null,"Human-curated datasets are also a bottleneck for research efficiency. Other areas of ML such as Natural Language\nProcessing don't have this bottleneck, and MV has FOMO w.r.t. NLP!"),(0,a.kt)("p",null,(0,a.kt)("img",{alt:"automate",src:r(8525).Z,width:"938",height:"599"})),(0,a.kt)("p",null,"Brobot's automation framework provides a good basis for building a labeling tool",(0,a.kt)("br",{parentName:"p"}),"\n",(0,a.kt)("em",{parentName:"p"},"(a \u2713 means Brobot now has this functionality, and a")," ",(0,a.kt)("strong",{parentName:"p"},"_")," ",(0,a.kt)("em",{parentName:"p"},"means that it's a work in progress)."),"  "),(0,a.kt)("p",null,"\u2713 Brobot's state-based automation",(0,a.kt)("br",{parentName:"p"}),"\n","\u2713 ",(0,a.kt)("a",{parentName:"p",href:"/brobot/docs/tutorial-basics/live-automation"},"Pattern matching"),(0,a.kt)("br",{parentName:"p"}),"\n","\u2713 Color matching",(0,a.kt)("br",{parentName:"p"}),"\n",(0,a.kt)("strong",{parentName:"p"},"_"),"  Movement",(0,a.kt)("br",{parentName:"p"}),"\n","...",(0,a.kt)("br",{parentName:"p"}),"\n",(0,a.kt)("strong",{parentName:"p"},"_"),"  Representation learning"))}f.isMDXComponent=!0},8525:(e,t,r)=>{r.d(t,{Z:()=>n});const n=r.p+"assets/images/automate-ad382ce4a943ce4cc8f5975bd73476c4.png"},882:(e,t,r)=>{r.d(t,{Z:()=>n});const n=r.p+"assets/images/google-c818c0317e83784671ca4a5af6ac3f74.png"},9903:(e,t,r)=>{r.d(t,{Z:()=>n});const n="data:image/png;base64,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"},4577:(e,t,r)=>{r.d(t,{Z:()=>n});const n=r.p+"assets/images/stanford-6ad45c11dcefd1dcd7e54bd3c6db4af4.png"}}]);