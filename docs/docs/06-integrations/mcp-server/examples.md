---
sidebar_position: 5
---

# AI Integration Examples

:::danger INCOMPLETE CODE EXAMPLES - FOR ILLUSTRATION ONLY

**These examples are incomplete and will not run as-is.** They demonstrate conceptual integration patterns but contain:

**Critical Issues**:
- **Incomplete implementations**: Many functions are stubs with `pass` statements
- **Deprecated APIs**: Some examples use outdated library versions (OpenAI &lt;1.0, LangChain &lt;1.0)
- **Undefined functions**: Placeholder functions (ai_process_instruction, ai_verify_state, etc.) are not implemented
- **Missing error handling**: Production code requires additional validation and error handling

**Only 1 of 6 major examples is complete enough to run** (AutoGPT Plugin). All others require significant modifications.

**Use these examples ONLY as starting points for your own implementations.** Refer to:
- [Installation Guide](./installation.md) for setup instructions
- [Getting Started](./getting-started.md) for basic usage patterns
- [Configuration Guide](./configuration.md) for server configuration
- [API Reference](./api-reference.md) for accurate method signatures

:::

Learn how to integrate popular AI services and frameworks with the Brobot MCP Server.

## OpenAI GPT Integration

### Basic GPT-4 Integration

```python
from openai import OpenAI
from brobot_client import BrobotClient
import json

# Initialize clients (requires openai>=1.0.0)
client = OpenAI(api_key="your-api-key")
brobot = BrobotClient()

def execute_natural_language_command(instruction: str):
    """Execute a natural language command using GPT-4.

    NOTE: This is an incomplete example. Production code needs:
    - JSON validation and error handling
    - Rate limiting and retry logic
    - Proper action parameter validation
    """

    # Get current screen state
    observation = brobot.get_observation()
    active_states = [s.name for s in observation.active_states]

    # Create prompt for GPT-4
    prompt = f"""
    Current application state: {active_states}
    User instruction: {instruction}

    Available actions:
    - click(image_pattern) - Click on UI element
    - type_text(text) - Type text
    - wait_for_state(state_name) - Wait for state

    Respond with a JSON array of actions to execute.
    Example: [{{"action": "click", "params": {{"pattern": "login_btn.png"}}}}]
    """

    # Get GPT-4 response
    response = client.chat.completions.create(
        model="gpt-4",
        messages=[
            {"role": "system", "content": "You are a UI automation assistant."},
            {"role": "user", "content": prompt}
        ],
        temperature=0.3
    )

    # Parse and execute actions
    actions = json.loads(response.choices[0].message.content)

    for action in actions:
        if action["action"] == "click":
            brobot.click(action["params"]["pattern"])
        elif action["action"] == "type_text":
            brobot.type_text(action["params"]["text"])
        elif action["action"] == "wait_for_state":
            brobot.wait_for_state(action["params"]["state"])

# Example usage
execute_natural_language_command("Log into the application with username 'demo'")
```

### Vision-Enabled GPT-4V

```python
import base64
from openai import OpenAI

client = OpenAI()
brobot = BrobotClient()

def analyze_and_act():
    """Use GPT-4V to analyze screenshots and decide actions."""
    
    # Get observation with screenshot
    obs = brobot.get_observation()
    
    # Prepare image for GPT-4V
    image_base64 = obs.screenshot
    
    response = client.chat.completions.create(
        model="gpt-4-vision-preview",
        messages=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": "What UI elements do you see? What should I click to login?"
                    },
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": f"data:image/png;base64,{image_base64}"
                        }
                    }
                ]
            }
        ],
        max_tokens=300
    )
    
    # Execute suggested action
    suggestion = response.choices[0].message.content
    print(f"GPT-4V suggests: {suggestion}")
```

## Anthropic Claude Integration

### Claude 3 with Computer Use

```python
from anthropic import Anthropic
from brobot_client import BrobotClient
import asyncio

anthropic = Anthropic(api_key="your-api-key")
brobot = BrobotClient()

class ClaudeAutomationAgent:
    """Agent that uses Claude to control applications.

    NOTE: This is an incomplete example. The _execute_plan method
    needs to parse Claude's response and execute actions.
    """

    def __init__(self):
        self.conversation = []

    async def process_task(self, task: str):
        """Process a high-level task using Claude."""

        # Get current state
        obs = brobot.get_observation()

        # Build context
        context = f"""
        Task: {task}
        Current screen: {obs.active_states}
        Available actions: click, type, drag, wait

        Plan and execute the steps needed to complete this task.
        """

        response = anthropic.messages.create(
            model="claude-3-5-sonnet-20241022",  # Use latest model
            messages=[{"role": "user", "content": context}],
            max_tokens=1000
        )

        # Execute Claude's plan (INCOMPLETE - needs implementation)
        await self._execute_plan(response.content)

    async def _execute_plan(self, plan: str):
        """Parse and execute Claude's plan.

        TODO: Implement plan parsing and action execution.
        """
        pass

# Usage
agent = ClaudeAutomationAgent()
await agent.process_task("Create a new document and save it as 'report.pdf'")
```

### Interactive Claude Assistant

```python
def create_interactive_assistant():
    """Create an interactive automation assistant with Claude."""
    
    class InteractiveSession:
        def __init__(self):
            self.messages = []
            
        def chat(self, user_input: str):
            # Add context about current screen
            obs = brobot.get_observation()
            
            enhanced_input = f"""
            User: {user_input}
            
            Current application state: {obs.get_most_confident_state().name}
            Visible elements: {[s.name for s in obs.active_states]}
            """
            
            self.messages.append({"role": "user", "content": enhanced_input})
            
            response = anthropic.messages.create(
                model="claude-3-5-sonnet-20241022",
                messages=self.messages,
                max_tokens=1024
            )
            
            self.messages.append({"role": "assistant", "content": response.content})
            
            return response.content
    
    return InteractiveSession()

# Interactive usage
session = create_interactive_assistant()
print(session.chat("How do I navigate to settings?"))
print(session.chat("Now change the theme to dark mode"))
```

## LangChain Integration

### Brobot as LangChain Tool

```python
from langchain_core.tools import tool
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent
from brobot_client import BrobotClient

# NOTE: This example uses LangChain 1.0+ and LangGraph.
# For older LangChain versions, see deprecated initialize_agent() API.

# Create Brobot tools for LangChain
client = BrobotClient()

@tool
def observe_screen(query: str = "") -> str:
    """Observe current screen state and active UI elements."""
    obs = client.get_observation()
    states = [f"{s.name} ({s.confidence:.0%})" for s in obs.active_states]
    return f"Active states: {', '.join(states)}"

@tool
def click_element(pattern: str) -> str:
    """Click on UI element by image pattern name."""
    try:
        result = client.click(pattern)
        return f"Clicked {pattern} successfully"
    except Exception as e:
        return f"Failed to click {pattern}: {str(e)}"

@tool
def type_text_tool(text: str) -> str:
    """Type text into current field."""
    result = client.type_text(text)
    return f"Typed '{text}'"

# Create agent with LangGraph
llm = ChatOpenAI(temperature=0, model="gpt-4")
tools = [observe_screen, click_element, type_text_tool]
agent_executor = create_react_agent(llm, tools)

# Use agent
for chunk in agent_executor.stream(
    {"messages": [("human", "Log into the application with username 'demo@example.com'")]}
):
    print(chunk)
    print("----")
```

### Custom LangChain Chain

```python
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_openai import ChatOpenAI

class BrobotAutomationChain:
    """Custom chain for complex automations.

    NOTE: This is an incomplete example. The execute_task method
    has stub implementations that need to be completed.
    """

    def __init__(self):
        self.brobot = BrobotClient()

        # Create prompt template
        self.planner_prompt = ChatPromptTemplate.from_messages([
            ("system", "You are an automation planning assistant."),
            ("human", """Task: {task}
Current State: {current_state}

Create a step-by-step plan to complete this task.
Format: numbered list of actions""")
        ])

        # Create chain with LCEL (LangChain Expression Language)
        llm = ChatOpenAI(temperature=0.3, model="gpt-4")
        self.planner = self.planner_prompt | llm | StrOutputParser()

    def execute_task(self, task: str):
        # Get current state
        obs = self.brobot.get_observation()
        current_state = obs.get_most_confident_state().name

        # Generate plan
        plan = self.planner.invoke({"task": task, "current_state": current_state})

        # Execute plan steps (INCOMPLETE - needs implementation)
        for step in plan.split('\n'):
            if 'click' in step.lower():
                # TODO: Extract pattern from step and click
                pass
            elif 'type' in step.lower():
                # TODO: Extract text from step and type
                pass

# Usage
chain = BrobotAutomationChain()
chain.execute_task("Create a new invoice for $1,500")
```

## AutoGPT/Agent Frameworks

### AutoGPT Plugin

```python
class BrobotAutoGPTPlugin:
    """Plugin to give AutoGPT UI control capabilities."""
    
    def __init__(self):
        self.client = BrobotClient()
        
    def get_commands(self):
        return {
            "ui_observe": self.observe,
            "ui_click": self.click,
            "ui_type": self.type_text,
            "ui_wait": self.wait_for_state
        }
    
    def observe(self) -> dict:
        """Observe current UI state."""
        obs = self.client.get_observation()
        return {
            "states": [s.name for s in obs.active_states],
            "screenshot_available": bool(obs.screenshot)
        }
    
    def click(self, target: str) -> dict:
        """Click UI element."""
        try:
            self.client.click(target)
            return {"success": True, "message": f"Clicked {target}"}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def type_text(self, text: str) -> dict:
        """Type text."""
        self.client.type_text(text)
        return {"success": True, "message": f"Typed: {text}"}
    
    def wait_for_state(self, state: str, timeout: float = 10) -> dict:
        """Wait for specific state."""
        try:
            self.client.wait_for_state(state, timeout)
            return {"success": True, "message": f"Reached state: {state}"}
        except:
            return {"success": False, "error": "Timeout waiting for state"}
```

## Multi-Agent Systems

### Coordinator-Worker Pattern

```python
import asyncio
from typing import List, Dict

class AutomationCoordinator:
    """Coordinates multiple AI agents for complex tasks."""
    
    def __init__(self):
        self.brobot = BrobotClient()
        self.observer_agent = ObserverAgent()
        self.planner_agent = PlannerAgent()
        self.executor_agent = ExecutorAgent()
    
    async def execute_complex_task(self, task: str):
        # Observer analyzes current state
        state_analysis = await self.observer_agent.analyze(self.brobot)
        
        # Planner creates execution plan
        plan = await self.planner_agent.create_plan(task, state_analysis)
        
        # Executor carries out plan
        results = await self.executor_agent.execute(plan, self.brobot)
        
        return results

class ObserverAgent:
    """Specialized in understanding UI state.

    NOTE: This is an incomplete example. Methods _identify_screen,
    _find_actionable_elements, and _identify_navigation are not implemented.
    """

    async def analyze(self, brobot: BrobotClient) -> Dict:
        obs = brobot.get_observation()

        # Use AI to analyze screenshot and states (INCOMPLETE)
        analysis = {
            "current_screen": self._identify_screen(obs),
            "available_actions": self._find_actionable_elements(obs),
            "navigation_options": self._identify_navigation(obs)
        }

        return analysis

    def _identify_screen(self, obs) -> str:
        """TODO: Implement screen identification."""
        pass

    def _find_actionable_elements(self, obs) -> list:
        """TODO: Implement actionable element detection."""
        pass

    def _identify_navigation(self, obs) -> list:
        """TODO: Implement navigation option detection."""
        pass

class PlannerAgent:
    """Creates execution plans.

    NOTE: This is a stub implementation.
    """

    async def create_plan(self, task: str, state: Dict) -> List[Dict]:
        """TODO: Use AI to create step-by-step plan."""
        pass

class ExecutorAgent:
    """Executes plans reliably.

    NOTE: This is an incomplete example. _execute_step is not implemented.
    """

    async def execute(self, plan: List[Dict], brobot: BrobotClient):
        results = []
        for step in plan:
            result = await self._execute_step(step, brobot)
            results.append(result)

            if not result["success"]:
                # Handle failures
                break

        return results

    async def _execute_step(self, step: Dict, brobot: BrobotClient) -> Dict:
        """TODO: Implement step execution."""
        pass
```

## Best Practices

### 1. Error Handling

```python
def safe_automation(instruction: str):
    """Automation with comprehensive error handling.

    NOTE: This is an incomplete example. Functions ai_process_instruction,
    execute_action, ai_plan_recovery, and ai_should_retry are not defined.
    """
    max_retries = 3

    for attempt in range(max_retries):
        try:
            # Get current state
            obs = brobot.get_observation()

            # AI processes instruction (PLACEHOLDER - not implemented)
            actions = ai_process_instruction(instruction, obs)

            # Execute with validation
            for action in actions:
                result = execute_action(action)  # PLACEHOLDER - not implemented
                if not result.success:
                    # AI decides how to recover (PLACEHOLDER - not implemented)
                    recovery = ai_plan_recovery(action, result.error)
                    execute_action(recovery)

            return True

        except Exception as e:
            if attempt < max_retries - 1:
                # Let AI decide if we should retry (PLACEHOLDER - not implemented)
                should_retry = ai_should_retry(e, attempt)
                if not should_retry:
                    break
            else:
                raise
```

### 2. State Verification

```python
import time

def verify_state_transition(expected_state: str, timeout: float = 10):
    """Verify state transitions with AI assistance.

    NOTE: This is an incomplete example. Functions ai_verify_state,
    ai_suggest_correction, and execute_action are not defined.
    """
    start_time = time.time()

    while time.time() - start_time < timeout:
        obs = brobot.get_observation()

        # AI verifies if we're in expected state (PLACEHOLDER - not implemented)
        is_correct = ai_verify_state(obs, expected_state)

        if is_correct:
            return True

        # AI suggests corrective action (PLACEHOLDER - not implemented)
        correction = ai_suggest_correction(obs, expected_state)
        if correction:
            execute_action(correction)  # PLACEHOLDER - not implemented

        time.sleep(1)

    return False
```

### 3. Context Management

```python
class ContextAwareAutomation:
    """Maintains context across automation sessions.

    NOTE: This is an incomplete example. Function ai_execute is not defined.
    """

    def __init__(self):
        self.context = {
            "application": None,
            "user": None,
            "task_history": [],
            "state_history": []
        }

    def execute_with_context(self, task: str):
        # Add current context to AI prompt
        enhanced_task = f"""
        Task: {task}
        Application: {self.context['application']}
        Previous tasks: {self.context['task_history'][-5:]}
        """

        result = ai_execute(enhanced_task)  # PLACEHOLDER - not implemented

        # Update context
        self.context['task_history'].append(task)
        return result
```

## Performance Optimization

### Caching AI Decisions

```python
from functools import lru_cache
import hashlib
import json

class CachedAIAutomation:
    """Cache AI decisions for repeated scenarios.

    NOTE: This is an incomplete example. ai_model.decide and
    execute_decision functions are not defined.
    """

    @lru_cache(maxsize=100)
    def get_ai_decision(self, state_hash: str, task: str):
        """Cache AI decisions based on state and task."""
        return ai_model.decide(state_hash, task)  # PLACEHOLDER - not implemented

    def execute_task(self, task: str):
        obs = brobot.get_observation()

        # Create hash of current state
        state_data = {
            "states": [s.name for s in obs.active_states],
            "screen_size": (obs.screen_width, obs.screen_height)
        }
        state_hash = hashlib.md5(
            json.dumps(state_data, sort_keys=True).encode()
        ).hexdigest()

        # Get cached or new decision
        decision = self.get_ai_decision(state_hash, task)

        # Execute decision (PLACEHOLDER - not implemented)
        return execute_decision(decision)
```

### Parallel Processing

```python
import asyncio

async def parallel_ui_analysis():
    """Analyze UI using multiple AI models in parallel.

    NOTE: This is an incomplete example. Functions gpt_analyze_ui,
    claude_analyze_ui, local_model_analyze, and combine_ai_insights
    are not defined.
    """

    async def gpt_analysis():
        return await gpt_analyze_ui(brobot.get_observation())  # PLACEHOLDER

    async def claude_analysis():
        return await claude_analyze_ui(brobot.get_observation())  # PLACEHOLDER

    async def local_model_analysis():
        return await local_model_analyze(brobot.get_observation())  # PLACEHOLDER

    # Run all analyses in parallel
    results = await asyncio.gather(
        gpt_analysis(),
        claude_analysis(),
        local_model_analysis()
    )

    # Combine insights (PLACEHOLDER - not implemented)
    return combine_ai_insights(results)
```

## Next Steps

After reviewing these conceptual examples:

- 📖 Read the [API Reference](./api-reference.md) for accurate endpoint documentation
- 🚀 Follow the [Getting Started](./getting-started.md) guide for working examples
- ⚙️ Review [Configuration](./configuration.md) for server setup
- 🐛 Check [Troubleshooting](./troubleshooting.md) for common issues
- 💬 Report issues on [GitHub](https://github.com/jspinak/brobot-mcp-server/issues)

**Remember**: These examples are incomplete. Use them as starting points and refer to the API Reference for accurate method signatures.