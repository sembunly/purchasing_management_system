document.addEventListener("DOMContentLoaded", () => {
  const available = document.getElementById("availableUsers");
  const members = document.getElementById("groupMembers");
  const form = document.getElementById("groupForm");
  if (available && members && form) {
    document.querySelectorAll("[data-transfer]").forEach(button => button.addEventListener("click", () => {
      const from = button.dataset.transfer === "add" ? available : members;
      const to = button.dataset.transfer === "add" ? members : available;
      Array.from(from.selectedOptions).forEach(option => { option.selected = false; to.add(option); });
    }));
    form.addEventListener("submit", () => {
      const container = document.getElementById("memberIds");
      container.replaceChildren();
      const ready = document.createElement("input");
      ready.type = "hidden"; ready.name = "membersSubmitted"; ready.value = "true";
      container.append(ready);
      Array.from(members.options).forEach(option => {
        const input = document.createElement("input");
        input.type = "hidden"; input.name = "memberIds"; input.value = option.value;
        container.append(input);
      });
    });
  }
  document.querySelector("[data-expand-all]")?.addEventListener("click", event => {
    const nodes = document.querySelectorAll(".permission-tree details");
    const expand = Array.from(nodes).some(node => !node.open);
    nodes.forEach(node => { node.open = expand; });
    event.currentTarget.textContent = expand ? "Collapse all" : "Expand all";
  });
  const permissionForm = document.getElementById("groupForm");
  if (permissionForm) {
    const allRights = permissionForm.querySelector("[data-all-rights]");
    const actions = Array.from(permissionForm.querySelectorAll('input[name="allowed"]'));
    const sync = (parent, children) => {
      parent.checked = children.length > 0 && children.every(item => item.checked);
      parent.indeterminate = children.some(item => item.checked) && !parent.checked;
    };
    const syncAll = () => {
      permissionForm.querySelectorAll("[data-module-rights]").forEach(parent =>
        sync(parent, Array.from(parent.closest(".permission-actions").querySelectorAll('input[name="allowed"]'))));
      permissionForm.querySelectorAll("[data-type-rights]").forEach(parent =>
        sync(parent, Array.from(parent.closest(".permission-tree").querySelectorAll('input[name="allowed"]'))));
      sync(allRights, actions);
    };
    allRights.addEventListener("change", () => {
      actions.forEach(item => { item.checked = allRights.checked; });
      syncAll();
    });
    permissionForm.querySelectorAll("[data-module-rights]").forEach(parent =>
      parent.addEventListener("change", () => {
        parent.closest(".permission-actions").querySelectorAll('input[name="allowed"]').forEach(item => {
          item.checked = parent.checked;
        });
        syncAll();
      }));
    permissionForm.querySelectorAll("[data-type-rights]").forEach(parent =>
      parent.addEventListener("change", () => {
        parent.closest(".permission-tree").querySelectorAll('input[name="allowed"]').forEach(item => {
          item.checked = parent.checked;
        });
        syncAll();
      }));
    actions.forEach(item => item.addEventListener("change", syncAll));
    syncAll();
  }
});
