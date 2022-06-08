"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[2029],{3905:function(e,t,n){n.d(t,{Zo:function(){return u},kt:function(){return f}});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var c=r.createContext({}),l=function(e){var t=r.useContext(c),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},u=function(e){var t=l(e.components);return r.createElement(c.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},p=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,i=e.originalType,c=e.parentName,u=s(e,["components","mdxType","originalType","parentName"]),p=l(n),f=o,h=p["".concat(c,".").concat(f)]||p[f]||d[f]||i;return n?r.createElement(h,a(a({ref:t},u),{},{components:n})):r.createElement(h,a({ref:t},u))}));function f(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var i=n.length,a=new Array(i);a[0]=p;var s={};for(var c in t)hasOwnProperty.call(t,c)&&(s[c]=t[c]);s.originalType=e,s.mdxType="string"==typeof e?e:o,a[1]=s;for(var l=2;l<i;l++)a[l]=n[l];return r.createElement.apply(null,a)}return r.createElement.apply(null,n)}p.displayName="MDXCreateElement"},2699:function(e,t,n){n.r(t),n.d(t,{assets:function(){return u},contentTitle:function(){return c},default:function(){return f},frontMatter:function(){return s},metadata:function(){return l},toc:function(){return d}});var r=n(7462),o=n(3366),i=(n(7294),n(3905)),a=["components"],s={sidebar_position:8},c="Action Recording",l={unversionedId:"introduction/core-features/action-recording",id:"introduction/core-features/action-recording",title:"Action Recording",description:"Brobot has 3 types of testing: integration testing, unit testing, and",source:"@site/docs/introduction/core-features/action-recording.md",sourceDirName:"introduction/core-features",slug:"/introduction/core-features/action-recording",permalink:"/brobot/docs/introduction/core-features/action-recording",draft:!1,editUrl:"https://jspinak.github.io/brobot/docs/introduction/core-features/action-recording.md",tags:[],version:"current",sidebarPosition:8,frontMatter:{sidebar_position:8},sidebar:"tutorialSidebar",previous:{title:"State Management",permalink:"/brobot/docs/introduction/core-features/state-management"},next:{title:"Origin of the Name Brobot",permalink:"/brobot/docs/introduction/name-origin"}},u={},d=[],p={toc:d};function f(e){var t=e.components,s=(0,o.Z)(e,a);return(0,i.kt)("wrapper",(0,r.Z)({},p,s,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h1",{id:"action-recording"},"Action Recording"),(0,i.kt)("p",null,"Brobot has 3 types of testing: integration testing, unit testing, and\naction recording. Their primary strengths are the following:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"Integration testing: making the application more robust in an inherently stochastic\nenvironment (reducing the variability of results).  "),(0,i.kt)("li",{parentName:"ul"},"Unit testing: making sure that parts of the application work as expected (increasing\nthe accuracy of results).  "),(0,i.kt)("li",{parentName:"ul"},"Action recording: similar to unit testing, focused on the accuracy of results, but\nperformed visually.  ")),(0,i.kt)("p",null,"Action recording takes care of a weakness inherent in unit testing. Unit testing requires\nan exact calculation of the expected results, which can be very time-consuming.\nImaging having a Find.ALL operation that returns 20 results. Finding the correct\nlocations of all 20 matches can take a while, and doing it for an entire series of\noperations can take too long for it to be considered a productive activity. Action\nrecording, on the other hand, requires no preparation and gives a visual representation of\naction results. Matches, mouse movement, clicks, and drags are all illustrated on screenshots\ntaken when the actions were performed. These illustrated screenshots can then be scanned\nto see if the actions performed as expected.  "),(0,i.kt)("p",null,"Illustrated screenshots and the original screenshots (without the illustration) are\nboth saved to the folder specified by ",(0,i.kt)("inlineCode",{parentName:"p"},"BrobotSettings.historyPath"),". The default value\nis ",(0,i.kt)("inlineCode",{parentName:"p"},"history/"),", which refers to a folder called ",(0,i.kt)("em",{parentName:"p"},"history")," in the root project directory.\nFor illustrated screenshots, ",(0,i.kt)("inlineCode",{parentName:"p"},"BrobotSettings.historyFilename")," gives the base name\nof the files to be saved. The default base name is ",(0,i.kt)("inlineCode",{parentName:"p"},"hist"),". For original screenshots,\nthe base name is given by the same variable that specifies the base name for\nscreenshots saved for the State Structure builder: ",(0,i.kt)("inlineCode",{parentName:"p"},"BrobotSettings.screenshotFilename"),".\nThe default is ",(0,i.kt)("inlineCode",{parentName:"p"},"screen"),". Illustrated screenshots are saved in the format:\n",(0,i.kt)("em",{parentName:"p"},"historyFilename#-ACTION-objectActedOn"),". Original screenshots are saved in the format:\n",(0,i.kt)("em",{parentName:"p"},"screenshotFilename#"),". A history containing two actions might look like this (these are\nfilenames in the history folder):"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre"},"    hist1-CLICK-image2.png\n    screen1.png\n    hist0-FIND-image1.png\n    screen0.png\n")),(0,i.kt)("p",null,"The below image is the illustrated screenshot of a Find operation:"),(0,i.kt)("p",null,(0,i.kt)("img",{alt:"illustrated find",src:n(4008).Z,width:"302",height:"264"})),(0,i.kt)("p",null,"The following image shows the illustrated screenshot of a Move (mouse move) operation:"),(0,i.kt)("p",null,(0,i.kt)("img",{alt:"illustrated move",src:n(690).Z,width:"652",height:"446"})))}f.isMDXComponent=!0},4008:function(e,t,n){t.Z=n.p+"assets/images/illustrated-find-642f1d76e77dad75fb33bd1e968889b7.png"},690:function(e,t,n){t.Z=n.p+"assets/images/illustrated-move-5ff1613eb0fb0bb12a172c4ac10a6eac.png"}}]);