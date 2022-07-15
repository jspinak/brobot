"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[2214],{3905:function(e,n,t){t.d(n,{Zo:function(){return l},kt:function(){return f}});var o=t(7294);function i(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function r(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);n&&(o=o.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,o)}return t}function a(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?r(Object(t),!0).forEach((function(n){i(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):r(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function s(e,n){if(null==e)return{};var t,o,i=function(e,n){if(null==e)return{};var t,o,i={},r=Object.keys(e);for(o=0;o<r.length;o++)t=r[o],n.indexOf(t)>=0||(i[t]=e[t]);return i}(e,n);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(o=0;o<r.length;o++)t=r[o],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(i[t]=e[t])}return i}var d=o.createContext({}),c=function(e){var n=o.useContext(d),t=n;return e&&(t="function"==typeof e?e(n):a(a({},n),e)),t},l=function(e){var n=c(e.components);return o.createElement(d.Provider,{value:n},e.children)},u={inlineCode:"code",wrapper:function(e){var n=e.children;return o.createElement(o.Fragment,{},n)}},p=o.forwardRef((function(e,n){var t=e.components,i=e.mdxType,r=e.originalType,d=e.parentName,l=s(e,["components","mdxType","originalType","parentName"]),p=c(t),f=i,h=p["".concat(d,".").concat(f)]||p[f]||u[f]||r;return t?o.createElement(h,a(a({ref:n},l),{},{components:t})):o.createElement(h,a({ref:n},l))}));function f(e,n){var t=arguments,i=n&&n.mdxType;if("string"==typeof e||i){var r=t.length,a=new Array(r);a[0]=p;var s={};for(var d in n)hasOwnProperty.call(n,d)&&(s[d]=n[d]);s.originalType=e,s.mdxType="string"==typeof e?e:i,a[1]=s;for(var c=2;c<r;c++)a[c]=t[c];return o.createElement.apply(null,a)}return o.createElement.apply(null,t)}p.displayName="MDXCreateElement"},6690:function(e,n,t){t.r(n),t.d(n,{assets:function(){return l},contentTitle:function(){return d},default:function(){return f},frontMatter:function(){return s},metadata:function(){return c},toc:function(){return u}});var o=t(7462),i=t(3366),r=(t(7294),t(3905)),a=["components"],s={sidebar_position:2},d="Combining Find Operations",c={unversionedId:"introduction/finding-objects/combining-finds",id:"introduction/finding-objects/combining-finds",title:"Combining Find Operations",description:"Combining multiple find operations in the same Action can give us better results.",source:"@site/docs/introduction/finding-objects/combining-finds.md",sourceDirName:"introduction/finding-objects",slug:"/introduction/finding-objects/combining-finds",permalink:"/brobot/docs/introduction/finding-objects/combining-finds",draft:!1,editUrl:"https://jspinak.github.io/brobot/docs/introduction/finding-objects/combining-finds.md",tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"tutorialSidebar",previous:{title:"Using Color",permalink:"/brobot/docs/introduction/finding-objects/using-color"},next:{title:"Origin of the Name Brobot",permalink:"/brobot/docs/introduction/name-origin"}},l={},u=[{value:"Nested Finds",id:"nested-finds",level:2},{value:"Confirmed Finds",id:"confirmed-finds",level:2}],p={toc:u};function f(e){var n=e.components,s=(0,i.Z)(e,a);return(0,r.kt)("wrapper",(0,o.Z)({},p,s,{components:n,mdxType:"MDXLayout"}),(0,r.kt)("h1",{id:"combining-find-operations"},"Combining Find Operations"),(0,r.kt)("p",null,"Combining multiple find operations in the same Action can give us better results.\nThere are two ways to do this with Brobot: Nested Finds, and Confirmed Finds. Both\nmethods require multiple Find operations to be added to the ActionOptions object,\nand call the Find operations in the order they were added to the ActionOptions. As an\nexample, when using the following ActionOptions, the Find.ALL operation would be called\nfirst, and then the Find.COLOR operation:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"    ActionOptions color = new ActionOptions.Builder()\n            .setAction(ActionOptions.Action.FIND)\n            .setFind(ActionOptions.Find.ALL)\n            .addFind(ActionOptions.Find.COLOR)\n            .build();\n")),(0,r.kt)("p",null,"Combining find methods can give us more accurate matches in scenarios where the\nform and color of an object are not unique. Take the example below, where we are looking\nfor the yellow bars above the kobolds (the top-left bar has blue circles on it).\nA relatively solid bar of color will correspond to other places on the screen, including\nthe green and red bars above the character. On the other hand, the yellow color of\nthe bars would also be found in other places, including on the character's weapon and\ninterspersed throughout the grass. One way to narrow down our search is to look for\nboth a pattern and a color.  "),(0,r.kt)("p",null,(0,r.kt)("img",{alt:"yellowBars",src:t(8021).Z,width:"659",height:"368"})),(0,r.kt)("h2",{id:"nested-finds"},"Nested Finds"),(0,r.kt)("p",null,"Nested Finds find objects inside the matches from the previous Find operation. Given\nthe example above, we would have many matches inside the four yellow bars. The\nActionOptions in the example uses the default diameter of 1 for the Find.COLOR operation.\nTherefore, the matches returned would all have a size of 1x1.  "),(0,r.kt)("p",null,"The ActionOptions variable ",(0,r.kt)("inlineCode",{parentName:"p"},"keepLargerMatches")," controls whether the Find operations\nshould be Nested Finds or ConfirmedFinds. The default value of ",(0,r.kt)("inlineCode",{parentName:"p"},"false")," will execute a\nNested Find.  "),(0,r.kt)("h2",{id:"confirmed-finds"},"Confirmed Finds"),(0,r.kt)("p",null,"Confirmed Finds look for matches inside the matches from the first Find operation.\nAll subsequent Find operations are performed on the match regions from the first operation.\nIf a match is found, the match region from the first Find operation will be returned.\nFor a match to exist, all subsequent Find operations need to succeed within its region.\nIn the example above, if a yellow pixel was found in the match region of a solid color bar,\nthe entire bar would be returned as a match object. The size of the match would equal\nthe size of the bar image on file.  "),(0,r.kt)("p",null,"To set the Find operations to Confirmed Finds, the ActionOptions variable\n",(0,r.kt)("inlineCode",{parentName:"p"},"keepLargerMatches")," should be set to true.  "),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre"},"    ActionOptions color = new ActionOptions.Builder()\n            .setAction(ActionOptions.Action.FIND)\n            .setFind(ActionOptions.Find.ALL)\n            .addFind(ActionOptions.Find.COLOR)\n            .keepLargerMatches(true)\n            .build();\n")))}f.isMDXComponent=!0},8021:function(e,n,t){n.Z=t.p+"assets/images/yellowBars-1ed8ee2ae5a22cb82150c83569176bd5.png"}}]);